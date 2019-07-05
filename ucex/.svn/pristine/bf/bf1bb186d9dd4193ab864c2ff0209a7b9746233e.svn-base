package api.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStreamReader extends Thread {
	private static Logger	log = LoggerFactory.getLogger(ProcessStreamReader.class);
	
	private InputStream		inStream = null;
	private boolean			isErrorStream = false;
	//
	private StringBuffer	outputBuffer = new StringBuffer();
	private BufferedReader	reader = null;

	public ProcessStreamReader(InputStream is, boolean isError) {
		inStream = is;
		isErrorStream = isError;

		reader = new BufferedReader(new InputStreamReader(inStream));
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeBufferReader(reader);
		closeInputStream(inStream);
	}

	@Override
	public void run() {
		readBuffer();
	}
	
	synchronized public StringBuffer getBuffer() {
		return outputBuffer;
	}

	private void closeInputStream(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.toString());
			} finally {
				is = null;
			}
		}
	}

	private void closeBufferReader(BufferedReader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.toString());
			} finally {
				reader = null;
			}
		}
	}

	protected void readBuffer() {
		String	stdLine;

		try {
			while ((stdLine = reader.readLine()) != null) {
				outputBuffer.append(stdLine);

				if(isErrorStream) {
					log.error("PROC OUTPUT >> " + stdLine);
				} else {
					log.info("PROC OUTPUT >> " + stdLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		} finally {
			closeBufferReader(reader);
			closeInputStream(inStream);
		}
	}

}
