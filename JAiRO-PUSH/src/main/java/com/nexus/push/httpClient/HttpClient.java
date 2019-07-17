package com.nexus.push.httpClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.internal.platform.ConscryptPlatform;
import okio.Buffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.coyote.http2.Http2AsyncUpgradeHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.Message;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nexus.push.domain.HttpStatusDomain;
import com.nexus.push.domain.PushDomain;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import static org.eclipse.jetty.util.ssl.SslContextFactory.TRUST_ALL_CERTS;
/**
 * An HTTP2 client that allows you to send HTTP2 frames to a server. Inbound and outbound frames are
 * logged. When run from the command-line, sends a single HEADERS frame to the server and gets back
 * a "Hello World" response.
 */
@Slf4j
@Component
public class HttpClient{

	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
//	private HashMap<String,Integer> multiPushResult = new HashMap<String, Integer>();
//	public HashMap<String,Integer> getMultiPushResult() {
//		return multiPushResult;
//	}
//
//	public void setMultiPushResult(boolean result) {
//		if(result) {
//			this.multiPushResult.put("success", multiPushResult.get("success")+1);
//		}else{
//			this.multiPushResult.put("fail", multiPushResult.get("fail")+1);
//		}
//	}
	@Autowired
	Environment env;
	@Autowired
	private HttpClientAsync hca;
	int success = 0;
	//연결 실패 시 재시도 횟수
	private int tryCount=0;
	
	public HttpStatusDomain http2Start(PushDomain pushDomain) throws Exception {	
		pushDomain.setApns_full_url(
        		pushDomain.getRequest_type()+
        		pushDomain.getApns_start_url()+
        		":"+pushDomain.getApns_port()+
        		pushDomain.getApns_end_url()+
        		pushDomain.getDevice_token()
			  );
		logger.info("PUSH HTTPclient START !!!!!");
    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",pushDomain.getDevice());
    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",pushDomain.getDevice_token());
    	logger.info("PUSH HTTPclient REQUEST URL : {}",pushDomain.getApns_full_url());
    	
    	InetSocketAddress addr=null;
    	EventLoopGroup workerGroup=null;
        Channel channel=null;
        Http2ClientInitializer initializer=null;
        Bootstrap b = null;
        Http2SettingsHandler http2SettingsHandler = null;
        HttpResponseHandler responseHandler = null;
    	try {
    		
    		addr=getInetSocketAddress(pushDomain);   	
    		final SslContext sslCtx;
	            SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
	            sslCtx = SslContextBuilder.forClient()
	                .sslProvider(provider)
	                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
	                .build();

	        workerGroup = new NioEventLoopGroup();
	        initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);
            // Configure the client.
            b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(addr);
            b.handler(initializer);
            
		    // Start the client.
		    channel = b.connect().syncUninterruptibly().channel();
		    // Wait for the HTTP/2 upgrade to occur.
            
		    http2SettingsHandler = initializer.settingsHandler();
	        http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);
	        
	        responseHandler = initializer.responseHandler();
	        int streamId = 3;
	        AsciiString hostName = new AsciiString(addr.toString());
	        logger.info("PUSH HTTPClient Sending request(s)...");
	            // Create a simple POST request with a body.
	            FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, pushDomain.getApns_full_url(),
	                    wrappedBuffer(pushDomain.getPost_data().getBytes(CharsetUtil.UTF_8)));
	            request.headers().add(HttpHeaderNames.HOST, hostName);
	            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
	            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
	            request.headers().set("apns-topic", pushDomain.getApns_topic());   
	            request.headers().set(HttpHeaderNames.AUTHORIZATION, "bearer "+pushDomain.getServer_token());
	            responseHandler.put(streamId, channel.write(request), channel.newPromise(), pushDomain.getDevice_token());
	        channel.flush();
	        responseHandler.awaitResponses(5, TimeUnit.SECONDS, null);
	        logger.info("PUSH HTTPClient Finished HTTP/2 request(s)");
	        
	        // Wait until the connection is closed.
	        channel.close().syncUninterruptibly();
	        logger.info("PUSH HTTPClient END !!!!!");
	        
        }catch(IllegalStateException e) {	
    		//네트워크 환경으로 인해 timeout일 경우 10회 재시도
        	if(e.getMessage().contains("Timed out")) {
        		if(tryCount == 10) throw e;
        		tryCount++;
            	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
            	channel.close();
            	workerGroup.shutdownGracefully();
            	Thread.sleep(1000);
            	http2Start(pushDomain);
        	}  	
        }catch(UnknownHostException e){
        	e.printStackTrace();
        	//네트워크 문제로 connection fail할 경우 10회 재 시도
        	if(tryCount == 10) throw e;
        	tryCount++;
        	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
        	channel.close();
        	workerGroup.shutdownGracefully();
        	Thread.sleep(1000);
        	http2Start(pushDomain);
        }finally {
        	workerGroup.shutdownGracefully();
        }
       

        return new HttpStatusDomain(responseHandler.getResponse_status(), responseHandler.getResponse_message());
    }
	
	public void http2MultiStart(PushDomain pushDomain) throws Exception {
		HashMap<String,Integer> multiPushResult = new HashMap<String, Integer>();
		multiPushResult.put("success", 0);
		multiPushResult.put("fail", 0);
		logger.info("요청 시작");
		long afterTime = 0;
		long secDiffTime = 0;
		long beforeTime = System.currentTimeMillis();
		pushDomain.setApns_full_url(
        		pushDomain.getRequest_type()+
        		pushDomain.getApns_start_url()+
        		":"+pushDomain.getApns_port()+
        		pushDomain.getApns_end_url()+
        		pushDomain.getDevice_token()
			  );
		logger.info("PUSH HTTPclient START !!!!!");
    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",pushDomain.getDevice());
    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",pushDomain.getDevice_token());
    	logger.info("PUSH HTTPclient REQUEST URL : {}",pushDomain.getApns_full_url());
    	
    	InetSocketAddress addr=null;
    	EventLoopGroup workerGroup=null;
        Channel channel=null;
        Http2ClientInitializer initializer=null;
        Bootstrap b = null;
        Http2SettingsHandler http2SettingsHandler = null;
        HttpResponseHandler responseHandler = null;
        ChannelFuture cf = null;
    	try {
    		
    		addr=getInetSocketAddress(pushDomain);   	
    		final SslContext sslCtx;
	            SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
	            sslCtx = SslContextBuilder.forClient()
	                .sslProvider(provider)
	                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
	                .build();

	        workerGroup = new NioEventLoopGroup();
	        initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);
            // Configure the client.
            b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(addr);
            b.handler(initializer);
		    // Start the client.
		    channel = b.connect().syncUninterruptibly().channel();
		    // Wait for the HTTP/2 upgrade to occur.
        //    cf=b.connect().awaitUninterruptibly();
		    http2SettingsHandler = initializer.settingsHandler();
	        http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);
	        
	        responseHandler = initializer.responseHandler();
	       
	        AsciiString hostName = new AsciiString(addr.toString());
	        logger.info("PUSH HTTPClient Sending request(s)...");
	            // Create a simple POST request with a body.
	        int streamId = 3;
	        for(int i=0;i<100;i++) {
	        	 
	        	logger.info("carrey : "+i);
	        	 FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, pushDomain.getApns_full_url(),
		                    wrappedBuffer(pushDomain.getPost_data().getBytes(CharsetUtil.UTF_8)));
		            request.headers().add(HttpHeaderNames.HOST, hostName);
		            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
		            request.headers().set("apns-topic", pushDomain.getApns_topic());   
		            request.headers().set(HttpHeaderNames.AUTHORIZATION, "bearer "+pushDomain.getServer_token());
		            responseHandler.put(streamId, channel.write(request), channel.newPromise(), pushDomain.getDevice_token());
		            streamId+=2;
		            channel.flush();
		            responseHandler.awaitResponses(5, TimeUnit.SECONDS, multiPushResult);
	        }
	       
	        logger.info("요청 끝");
			afterTime = System.currentTimeMillis(); 
			secDiffTime = (afterTime - beforeTime)/1000;
			logger.info("요청 처리 시간 : "+secDiffTime);
	        logger.info("PUSH HTTPClient Finished HTTP/2 request(s)");
	        
	        // Wait until the connection is closed.
	        channel.close().syncUninterruptibly();
	        logger.info("PUSH HTTPClient END !!!!!");
	        
        }catch(IllegalStateException e) {	
    		//네트워크 환경으로 인해 timeout일 경우 10회 재시도
        	if(e.getMessage().contains("Timed out")) {
        		if(tryCount == 10) throw e;
        		tryCount++;
            	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
            	channel.close();
            	workerGroup.shutdownGracefully();
            	Thread.sleep(1000);
            	http2Start(pushDomain);
        	}  	
        }catch(UnknownHostException e){
        	e.printStackTrace();
        	//네트워크 문제로 connection fail할 경우 10회 재 시도
        	if(tryCount == 10) throw e;
        	tryCount++;
        	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
        	channel.close();
        	workerGroup.shutdownGracefully();
        	Thread.sleep(1000);
        	http2Start(pushDomain);
        }finally {
        	workerGroup.shutdownGracefully();
        	logger.info("성공 개수 : "+multiPushResult.get("success"));
        	logger.info("실패 개수 : "+multiPushResult.get("fail"));
        }
    	
    }
	
	
	public void httpMultiStart(PushDomain pushDomain) throws InterruptedException{
		OkHttpClient client = new OkHttpClient.Builder()
    			//	.connectionSpecs(specs)
					.connectTimeout(60, TimeUnit.MINUTES)
					.retryOnConnectionFailure(true)
					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
					.build();
		Map<String, Integer> map = new HashMap<>();
		map.put("success",0);
		map.put("fail", 0);
		
		Request request = null;
		try {
			logger.info("시작");	
			long afterTime = 0;
			long secDiffTime = 0;
			long beforeTime = System.currentTimeMillis();
			//argument 값에 사용자 count를 넣으면 됨
			CountDownLatch cdl = new CountDownLatch(10000);
			for(int i=0;i<10000;i++) {
				request = new Request.Builder()
						.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
		        		.url(pushDomain.getFcm_full_url())
		        		.post(RequestBody.create(pushDomain.getPost_data(),JSON))
		        		.build();
				client.newCall(request).enqueue(new Callback() {
					
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						// TODO Auto-generated method stub
						
					//	logger.info("carrey : "+response.code());
						Buffer buffer = new Buffer();
						call.request().body().writeTo(buffer);
						
					//	logger.info("성공 : "+buffer.readUtf8());
					//	map.put("success", map.get("success")+1);	
						success++;
						cdl.countDown();
						response.close();
					}
					
					@Override
					public void onFailure(Call call, IOException e){
						// TODO Auto-generated method stub
						
						try {
							Buffer buffer = new Buffer();
							call.request().body().writeTo(buffer);
							logger.info("실패 : "+buffer.readUtf8());
						}catch(IOException e1) {
							e1.printStackTrace();
						}finally {
							map.put("fail", map.get("fail")+1);
							cdl.countDown();
						}
						
					}
				});
			}
			logger.info("종료");
			logger.info("요청 시간 : "+secDiffTime);
			afterTime = System.currentTimeMillis(); 
			secDiffTime = (afterTime - beforeTime)/1000;
			cdl.await();
			logger.info("성공 개수 : "+success);
			success=0;
//			for(String key:map.keySet()) {
//				logger.info(key+" : "+map.get(key));
//			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
//	public void httpMultiStart(PushDomain pushDomain) throws InterruptedException{
//		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
//		HttpEntity entity = new ByteArrayEntity(pushDomain.getPost_data().getBytes());
//		try {
//			httpclient.start();
//			final CountDownLatch latch = new CountDownLatch(300);
//			final HttpPost request = new HttpPost(pushDomain.getFcm_full_url());
//			request.addHeader("Authorization", "Bearer " + pushDomain.getServer_token());
//			request.addHeader("Content-Type", "application/json; UTF-8");
//			request.setEntity(entity);
//			
//			httpclient.execute(request, new FutureCallback<HttpResponse>() {
//				
//				@Override
//				public void failed(Exception ex) {
//					// TODO Auto-generated method stub
//					latch.countDown();
//				}
//				
//				@Override
//				public void completed(HttpResponse result) {
//					// TODO Auto-generated method stub
//					latch.countDown();
//				}
//				
//				@Override
//				public void cancelled() {
//					// TODO Auto-generated method stub
//					latch.countDown();
//				}
//			});
//		}catch(Exception e) {
//			
//		}
//		OkHttpClient client = new OkHttpClient.Builder()
//    			//	.connectionSpecs(specs)
//					.connectTimeout(60, TimeUnit.MINUTES)
//					.followRedirects(true)
//					.readTimeout(20, TimeUnit.MINUTES)
//					.retryOnConnectionFailure(false)
//					.writeTimeout(20, TimeUnit.MINUTES)
//					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
//					.build();
//		Map<String, Integer> map = new HashMap<>();
//		map.put("success",0);
//		map.put("fail", 0);
//		Request request = null;
//		try {
//			logger.info("시작");	
//			long afterTime = 0;
//			long secDiffTime = 0;
//			long beforeTime = System.currentTimeMillis();
//			//argument 값에 사용자 count를 넣으면 됨
//			CountDownLatch cdl = new CountDownLatch(300);
//			for(int i=0;i<300;i++) {
//				request = new Request.Builder()
//						.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
//		        		.addHeader("Content-Type", "application/json; UTF-8")
//		        		.url(pushDomain.getFcm_full_url())
//		        		.post(RequestBody.create(MediaType.parse("application/json"), pushDomain.getPost_data()))
//		        		.build();
//				client.newCall(request).enqueue(new Callback() {
//					
//					@Override
//					public void onResponse(Call call, Response response) throws IOException {
//						// TODO Auto-generated method stub
//						//logger.info("carrey : "+response.code());
//						Buffer buffer = new Buffer();
//						call.request().body().writeTo(buffer);
//					//	logger.info("성공 : "+buffer.readUtf8());
//						map.put("success", map.get("success")+1);
//						response.close();
//						cdl.countDown();
//					}
//					
//					@Override
//					public void onFailure(Call call, IOException e){
//						// TODO Auto-generated method stub
//						Buffer buffer = new Buffer();
//						try {
//							call.request().body().writeTo(buffer);
//						}catch(IOException e1) {
//							e1.printStackTrace();
//						}finally {
//							map.put("fail", map.get("fail")+1);
//							cdl.countDown();
//						}
//						
//					//	logger.info("실패 : "+buffer.readUtf8());
//						
//					}
//				});
//			}
//			logger.info("종료");
//			logger.info("요청 시간 : "+secDiffTime);
//			afterTime = System.currentTimeMillis(); 
//			secDiffTime = (afterTime - beforeTime)/1000;
//			cdl.await();
//			for(String key:map.keySet()) {
//				logger.info(key+" : "+map.get(key));
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		
//		
//	}
	public HttpStatusDomain httpStart(PushDomain pushDomain) throws Exception{	
		logger.info("PUSH HTTPclient START !!!!!");
    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",pushDomain.getDevice());
    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",pushDomain.getDevice_token());
    	logger.info("PUSH HTTPclient REQUEST URL : {}",pushDomain.getFcm_full_url());

    	//TLS1.2v 강제 셋팅
    	/*ConnectionSpec cs =new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
    			.tlsVersions(TlsVersion.TLS_1_2)
    			.build();
    	List<ConnectionSpec> specs = new ArrayList<>();
    	specs.add(cs);
    	specs.add(ConnectionSpec.COMPATIBLE_TLS);
    	specs.add(ConnectionSpec.CLEARTEXT);*/
    	OkHttpClient client = new OkHttpClient.Builder()
    			//	.connectionSpecs(specs)
					.connectTimeout(60, TimeUnit.MINUTES)
					.followRedirects(true)
					.readTimeout(20, TimeUnit.MINUTES)
					.retryOnConnectionFailure(false)
					.writeTimeout(20, TimeUnit.MINUTES)
					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
					.build();
			Request request = null;
			Response res = null;
			try {
				request = new Request.Builder()
		        		.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
		        		.addHeader("Content-Type", "application/json; UTF-8")
		        		.url(pushDomain.getFcm_full_url())
		        		.post(RequestBody.create(MediaType.parse("application/json"), pushDomain.getPost_data()))
		        		.build();
				res = client.newCall(request).execute();
			}catch(UnknownHostException e) {
				//네트워크 문제로 connection fail할 경우 10회 재 시도
				if(tryCount == 10) throw e;
	        	tryCount++;
	        	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
	        	Thread.sleep(1000);
	        	client.newCall(request).cancel();
	        	httpStart(pushDomain);
			}catch(Exception e) {
				e.printStackTrace();
			}
	        String response_message="";
	        if(res.code()!=200) {
	        	response_message=((JsonObject) new JsonParser()
						.parse(res.body().string()))
						.getAsJsonObject("error")
						.get("message").getAsString();
	        }
	        
    	return new HttpStatusDomain(res.code(), response_message);
	}
	
	public InetSocketAddress getInetSocketAddress(PushDomain pushDomain) {
		InetSocketAddress addr=InetSocketAddress.createUnresolved(pushDomain.getApns_start_url(), pushDomain.getApns_port());
		return addr;
	}

	

	
	
}