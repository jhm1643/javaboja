package api.push;

import static com.google.android.gcm.server.Constants.GCM_SEND_ENDPOINT;
import static com.google.android.gcm.server.Constants.PARAM_COLLAPSE_KEY;
import static com.google.android.gcm.server.Constants.PARAM_DELAY_WHILE_IDLE;
import static com.google.android.gcm.server.Constants.PARAM_PAYLOAD_PREFIX;
import static com.google.android.gcm.server.Constants.PARAM_REGISTRATION_ID;
import static com.google.android.gcm.server.Constants.PARAM_TIME_TO_LIVE;
import static com.google.android.gcm.server.Constants.TOKEN_CANONICAL_REG_ID;
import static com.google.android.gcm.server.Constants.TOKEN_ERROR;
import static com.google.android.gcm.server.Constants.TOKEN_MESSAGE_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StringUtils;

import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;

import api.push.GCMResult.Builder;

public class GCMSender {

	protected static final String UTF8 = "UTF-8";
	protected static final String MESSAGE_PRIORITY = "priority";
	protected static final int BACKOFF_INITIAL_DELAY = 1000;
	protected static final int MAX_BACKOFF_DELAY = 1024000;
 
	protected final Random random = new Random();
	protected final Logger logger = Logger.getLogger(getClass().getName());

	private final String key;

	public GCMSender(String key) {
		this.key = nonNull(key);
	}

	public GCMResult send(Message message, String registrationId, int retries, String priority) throws IOException {
		int attempt = 0;
		GCMResult result = null;
		int backoff = BACKOFF_INITIAL_DELAY;
		boolean tryAgain;
		
		do {
			attempt++;
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Attempt #" + attempt + " to send message " + message + " to regIds " + registrationId);
			}
			
			result = sendNoRetry(message, registrationId, priority);
			tryAgain = (result == null && attempt <= retries);
			
			if (tryAgain) {
				int sleepTime = backoff / 2 + random.nextInt(backoff);
				sleep(sleepTime);
				if (2 * backoff < MAX_BACKOFF_DELAY) {
					backoff *= 2;
				}
			}
		} while (tryAgain);
		
		if (result == null) {
			throw new IOException("Could not send message after " + attempt + " attempts");
		}
		
		return result;
	}

	public GCMResult sendNoRetry(Message message, String registrationId, String priority) throws IOException {

		StringBuilder body = newBody(PARAM_REGISTRATION_ID, registrationId);
		Boolean delayWhileIdle = message.isDelayWhileIdle();
		
		if (delayWhileIdle != null) {
			addParameter(body, PARAM_DELAY_WHILE_IDLE, delayWhileIdle ? "1" : "0");
		}
		
		String collapseKey = message.getCollapseKey();
		if (collapseKey != null) {
			addParameter(body, PARAM_COLLAPSE_KEY, collapseKey);
		}
		
		Integer timeToLive = message.getTimeToLive();
		if (timeToLive != null) {
			addParameter(body, PARAM_TIME_TO_LIVE, Integer.toString(timeToLive));
		}
		
		if ( !StringUtils.isEmpty(priority)) {
			addParameter(body, MESSAGE_PRIORITY, priority);
		}
		
		for (Entry<String, String> entry : message.getData().entrySet()) {
			String key = PARAM_PAYLOAD_PREFIX + entry.getKey();
			String value = entry.getValue();
			addParameter(body, key, URLEncoder.encode(value, UTF8));
		}
		
		String requestBody = body.toString();
		logger.finest("Request body: " + requestBody);
		
		//2018.12.07 martino GCM --> FCM push로 변경
		//HttpURLConnection conn = post(GCM_SEND_ENDPOINT, requestBody);
		HttpURLConnection conn = post("https://fcm.googleapis.com/fcm/send", requestBody);

		int status = conn.getResponseCode();
		
		if (status == 503) {
			logger.fine("GCM service is unavailable");
			return null;
		}
		
		if (status != 200) {
			throw new InvalidRequestException(status);
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			try {
				String line = reader.readLine();

				if (line == null || line.equals("")) {
					throw new IOException("Received empty response from GCM service.");
				}
				
				logger.log(Level.INFO, "Received from GCM#1: " + line);
				
				String[] responseParts = split(line);
				String token = responseParts[0];
				String value = responseParts[1];
				
				if (token.equals(TOKEN_MESSAGE_ID)) {
					Builder builder = new GCMResult.Builder().messageId(value);
					// check for canonical registration id
					line = reader.readLine();
					
					if (line != null) {
						logger.log(Level.INFO, "Received from GCM#2: " + line);
						
						responseParts = split(line);
						token = responseParts[0];
						value = responseParts[1];
						if (token.equals(TOKEN_CANONICAL_REG_ID)) {
							builder.canonicalRegistrationId(value);
						} else {
							logger.warning("Received invalid second line from GCM: " + line);
						}
					}

					GCMResult result = builder.build();
					//if (logger.isLoggable(Level.FINE)) {
					//	logger.fine("Message created succesfully (" + result + ")");
					//}
					logger.log(Level.INFO, "Message created succesfully (" + result + ")");
					return result;
				} else if (token.equals(TOKEN_ERROR)) {
					return new GCMResult.Builder().errorCode(value).build();
				} else {
					throw new IOException("Received invalid response from GCM: " + line);
				}
			} finally {
				reader.close();
			}
		} finally {
			conn.disconnect();
		}
	}

	private String[] split(String line) throws IOException {
		String[] split = line.split("=", 2);
		if (split.length != 2) {
			throw new IOException("Received invalid response line from GCM: " + line);
		}
		return split;
	}

	protected HttpURLConnection post(String url, String body) throws IOException {
		return post(url, "application/x-www-form-urlencoded;charset=UTF-8", body);
	}

	protected HttpURLConnection post(String url, String contentType, String body) throws IOException {
		if (url == null || body == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}
		
		if (!url.startsWith("https://")) {
			logger.warning("URL does not use https: " + url);
		}
		
		//logger.fine("Sending POST to " + url);
		//logger.finest("POST body: " + body);
		logger.log(Level.INFO, "Sending POST to " + url);
		logger.log(Level.INFO, "POST body: " + body);
		
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = getConnection(url);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setFixedLengthStreamingMode(bytes.length);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Authorization", "key=" + key);
		OutputStream out = conn.getOutputStream();
		out.write(bytes);
		out.close();
		return conn;
	}

	protected static final Map<String, String> newKeyValues(String key, String value) {
		Map<String, String> keyValues = new HashMap<String, String>(1);
		keyValues.put(nonNull(key), nonNull(value));
		return keyValues;
	}

	protected static StringBuilder newBody(String name, String value) {
		return new StringBuilder(nonNull(name)).append('=').append(nonNull(value));
	}

	protected static void addParameter(StringBuilder body, String name, String value) {
		nonNull(body)
			.append('&')
			.append(nonNull(name))
			.append('=')
			.append(nonNull(value));
	}

	protected HttpURLConnection getConnection(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		return conn;
	}

	protected static String getString(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(nonNull(stream)));
		StringBuilder content = new StringBuilder();
		String newLine;
		
		do {
			newLine = reader.readLine();
			if (newLine != null) {
				content.append(newLine).append('\n');
			}
		} while (newLine != null);
		
		if (content.length() > 0) {
			// strip last newline
			content.setLength(content.length() - 1);
		}
		
		return content.toString();
	}

	static <T> T nonNull(T argument) {
		if (argument == null) {
			throw new IllegalArgumentException("argument cannot be null");
		}
		return argument;
	}

	void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
