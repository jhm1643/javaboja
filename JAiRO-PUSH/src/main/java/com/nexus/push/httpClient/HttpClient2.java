//package com.nexus.push.httpClient;
//
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.ConnectionPool;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//import okio.Buffer;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.UnknownHostException;
//import java.security.Security;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import org.conscrypt.Conscrypt;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.nexus.push.domain.HttpResponseVo;
//import com.nexus.push.domain.HttpRequestVo;
//
//@Slf4j
//@Component
//public class HttpClient2{
//
//	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
//
//	@Autowired
//	Environment env;
//	int success = 0;
//	//연결 실패 시 재시도 횟수
//	private int tryCount=0;
//	
//	
//	public void http2MultiStart(HttpRequestVo pushDomain) throws Exception {
//		pushDomain.setApns_full_url(
//        		pushDomain.getRequest_type()+
//        		pushDomain.getApns_start_url()+
//        		":"+pushDomain.getApns_port()+
//        		pushDomain.getApns_end_url()+
//        		pushDomain.getDevice_token()
//			  );
//		logger.info("PUSH HTTPclient START !!!!!");
//    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",pushDomain.getDevice_type());
//    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",pushDomain.getDevice_token());
//    	logger.info("PUSH HTTPclient REQUEST URL : {}",pushDomain.getApns_full_url());
////    	final TrustManager[] trustAllCerts = new TrustManager[] {
////                new X509TrustManager() {
////                    @Override
////                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
////                    }
////
////                    @Override
////                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
////                    }
////
////                    @Override
////                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
////                        return new java.security.cert.X509Certificate[]{};
////                    }
////                }
////        };
////    	final SSLContext sslContext = SSLContext.getInstance("SSL");
////        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
////        
////        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
////    	List<Protocol> protocols = new ArrayList<Protocol>();
////    	protocols.add(Protocol.HTTP_2);
////    	protocols.add(Protocol.HTTP_1_1);
//    	Security.insertProviderAt(Conscrypt.newProvider(), 1);
//    	OkHttpClient client = new OkHttpClient.Builder()
//    			//	.connectionSpecs(specs)
//    			//	.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
////    				.hostnameVerifier(new HostnameVerifier() {
////						
////						@Override
////						public boolean verify(String hostname, SSLSession session) {
////							// TODO Auto-generated method stub
////							return true;
////						}
////					})
//    			//	.protocols(protocols)
//					.connectTimeout(60, TimeUnit.MINUTES)
//					.retryOnConnectionFailure(true)
//					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
//					.build();
//		Map<String, Integer> map = new HashMap<>();
//		map.put("success",0);
//		map.put("fail", 0);
//		
//		Request request = null;
//		try {
//			logger.info("요청 시작");	
//			long request_end_time = 0;
//			long response_end_time = 0;
//			long request_exec_time = 0;
//			long response_exec_time = 0;
//			long request_start_time = System.currentTimeMillis();
//			//argument 값에 사용자 count를 넣으면 됨
//			CountDownLatch cdl = new CountDownLatch(3000);
//			for(int i=0;i<3000;i++) {
//				request = new Request.Builder()
//						.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
//						.addHeader("apns-topic", pushDomain.getApns_topic())
//		        		.url(pushDomain.getApns_full_url())
//		        		.post(RequestBody.create(pushDomain.getPost_data(),JSON))
//		        		
//		        		.build();
//				client.newCall(request).enqueue(new Callback() {
//					
//					@Override
//					public void onResponse(Call call, Response response) throws IOException {
//						// TODO Auto-generated method stub
//						
////						Buffer buffer = new Buffer();
////						call.request().body().writeTo(buffer);
////						if(response.code()==200) {
////							//logger.info("성공 : "+buffer.readUtf8());
////							map.put("success", map.get("success")+1);	
////							success++;
////						}else {
////							logger.info("fail : "+response.code());
////						}
//						success++;
//						cdl.countDown();
//						response.close();
//					}
//					
//					@Override
//					public void onFailure(Call call, IOException e){
//						// TODO Auto-generated method stub
//						e.printStackTrace();
//						try {
//							Buffer buffer = new Buffer();
//							call.request().body().writeTo(buffer);
//							//logger.info("실패 : "+buffer.readUtf8());
//						}catch(IOException e1) {
//							e1.printStackTrace();
//						}finally {
//							map.put("fail", map.get("fail")+1);
//							cdl.countDown();
//						}
//						
//					}
//				});
//			}
//			request_end_time = System.currentTimeMillis(); 
//			request_exec_time = (request_end_time - request_start_time)/1000;
//			logger.info("요청 종료");
//			logger.info("요청 처리 시간 : "+request_exec_time);
//			cdl.await();
//			response_end_time = System.currentTimeMillis();
//			response_exec_time = (response_end_time - request_start_time)/1000;
//			logger.info("응답 완료 시간 : "+response_exec_time);
//			logger.info("성공 개수 : "+success);
//			success=0;
////			for(String key:map.keySet()) {
////				logger.info(key+" : "+map.get(key));
////			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//    }
//	
//	public void httpStart(HttpRequestVo pushDomain) throws InterruptedException{
//		OkHttpClient client = new OkHttpClient.Builder()
//    			//	.connectionSpecs(specs)
//					.connectTimeout(60, TimeUnit.MINUTES)
//					.retryOnConnectionFailure(true)
//					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
//					.build();
//		Map<String, Integer> map = new HashMap<>();
//		map.put("success",0);
//		map.put("fail", 0);
//		
//		Request request = null;
//		try {
//			logger.info("시작");	
//			long afterTime = 0;
//			long secDiffTime = 0;
//			long beforeTime = System.currentTimeMillis();
//			//argument 값에 사용자 count를 넣으면 됨
//			CountDownLatch cdl = new CountDownLatch(10000);
//			for(int i=0;i<10000;i++) {
//				request = new Request.Builder()
//						.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
//		        		.url(pushDomain.getFcm_full_url())
//		        		.post(RequestBody.create(pushDomain.getPost_data(),JSON))
//		        		.build();
//				client.newCall(request).enqueue(new Callback() {
//					
//					@Override
//					public void onResponse(Call call, Response response) throws IOException {
//						// TODO Auto-generated method stub
//						
//					//	logger.info("carrey : "+response.code());
//						Buffer buffer = new Buffer();
//						call.request().body().writeTo(buffer);
//						
//					//	logger.info("성공 : "+buffer.readUtf8());
//					//	map.put("success", map.get("success")+1);	
//						success++;
//						cdl.countDown();
//						response.close();
//					}
//					
//					@Override
//					public void onFailure(Call call, IOException e){
//						// TODO Auto-generated method stub
//						
//						try {
//							Buffer buffer = new Buffer();
//							call.request().body().writeTo(buffer);
//							logger.info("실패 : "+buffer.readUtf8());
//						}catch(IOException e1) {
//							e1.printStackTrace();
//						}finally {
//							map.put("fail", map.get("fail")+1);
//							cdl.countDown();
//						}
//						
//					}
//				});
//			}
//			logger.info("종료");
//			logger.info("요청 시간 : "+secDiffTime);
//			afterTime = System.currentTimeMillis(); 
//			secDiffTime = (afterTime - beforeTime)/1000;
//			cdl.await();
//			logger.info("성공 개수 : "+success);
//			success=0;
////			for(String key:map.keySet()) {
////				logger.info(key+" : "+map.get(key));
////			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		
//		
//	}
//
//	public HttpResponseVo httpStart(HttpRequestVo pushDomain) throws Exception{	
//		logger.info("PUSH HTTPclient START !!!!!");
//    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",pushDomain.getDevice_type());
//    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",pushDomain.getDevice_token());
//    	logger.info("PUSH HTTPclient REQUEST URL : {}",pushDomain.getFcm_full_url());
//
//    	OkHttpClient client = new OkHttpClient.Builder()
//					.connectTimeout(60, TimeUnit.MINUTES)
//					.followRedirects(true)
//					.readTimeout(20, TimeUnit.MINUTES)
//					.retryOnConnectionFailure(false)
//					.writeTimeout(20, TimeUnit.MINUTES)
//					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
//					.build();
//			Request request = null;
//			Response res = null;
//			try {
//				request = new Request.Builder()
//		        		.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
//		        		.addHeader("Content-Type", "application/json; UTF-8")
//		        		.url(pushDomain.getFcm_full_url())
//		        		.post(RequestBody.create(MediaType.parse("application/json"), pushDomain.getPost_data()))
//		        		.build();
//				res = client.newCall(request).execute();
//			}catch(UnknownHostException e) {
//				//네트워크 문제로 connection fail할 경우 10회 재 시도
//				if(tryCount == 10) throw e;
//	        	tryCount++;
//	        	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
//	        	Thread.sleep(1000);
//	        	client.newCall(request).cancel();
//	        	httpStart(pushDomain);
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//	        String response_message="";
//	        if(res.code()!=200) {
//	        	response_message=((JsonObject) new JsonParser()
//						.parse(res.body().string()))
//						.getAsJsonObject("error")
//						.get("message").getAsString();
//	        }
//	        
//    	return new HttpResponseVo(res.code(), response_message);
//	}
//	
//	public InetSocketAddress getInetSocketAddress(HttpRequestVo pushDomain) {
//		InetSocketAddress addr=InetSocketAddress.createUnresolved(pushDomain.getApns_start_url(), pushDomain.getApns_port());
//		return addr;
//	}
//
//	
//
//	
//	
//}