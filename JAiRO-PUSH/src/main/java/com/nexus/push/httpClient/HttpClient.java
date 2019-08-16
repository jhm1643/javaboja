package com.nexus.push.httpClient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.json.JSONParser;
import org.conscrypt.Conscrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.domain.HttpRequestVo;

@Slf4j
@Component
public class HttpClient implements Callback{

	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Autowired
	Environment env;

	private int tryCount=0;
	private CountDownLatch cdl;
	private int success_count=0;
	private int fail_count=0;
	HttpResponseVo pushResponseVo = new HttpResponseVo();
	
//	public void http2MultiStart(HttpRequestVo httpRequestVo) throws Exception {
//		httpRequestVo.setApns_full_url(
//        		httpRequestVo.getRequest_type()+
//        		httpRequestVo.getApns_start_url()+
//        		":"+httpRequestVo.getApns_port()+
//        		httpRequestVo.getApns_end_url()+
//        		httpRequestVo.getDevice_token()
//			  );
//		logger.info("PUSH HTTPclient START !!!!!");
//    	logger.info("PUSH HTTPclient DEVICE TYPE : {}",httpRequestVo.getDevice_type());
//    	logger.info("PUSH HTTPclient DEVICE TOKEN : {}",httpRequestVo.getDevice_token());
//    	logger.info("PUSH HTTPclient REQUEST URL : {}",httpRequestVo.getApns_full_url());
//
//    	Security.insertProviderAt(Conscrypt.newProvider(), 1);
//    	OkHttpClient client = new OkHttpClient.Builder()
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
//			cdl = new CountDownLatch(3000);
//			for(int i=0;i<3000;i++) {
//				request = new Request.Builder()
//						.addHeader("Authorization", "Bearer " + httpRequestVo.getServer_token())
//						.addHeader("apns-topic", httpRequestVo.getApns_topic())
//		        		.url(httpRequestVo.getApns_full_url())
//		        		.post(RequestBody.create(httpRequestVo.getPost_data(),JSON))
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
	
	public HttpResponseVo singlePushStart(HttpRequestVo httpRequestVo) throws Exception{
		logger.info("device_token : "+httpRequestVo.getDevice_token());
		logger.info("request url : "+httpRequestVo.getRequest_url());
		logger.info("device_type : "+httpRequestVo.getDevice_type());
		//HTTP2 setting for use
		Security.insertProviderAt(Conscrypt.newProvider(), 1);
		
		//HttpClient setting
		OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.MINUTES)
					.retryOnConnectionFailure(true)
					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
					.build();
		
		//Http header & body setting
		Builder builder = new Builder()
					.addHeader("Authorization", "Bearer " + httpRequestVo.getServer_token())
					.post(RequestBody.create(httpRequestVo.getPost_data(),JSON));
		
		//ios setting
		if(httpRequestVo.getDevice_type().equals("ios")) {
			builder.addHeader("apns-topic", httpRequestVo.getApns_topic())
					.url(httpRequestVo.getRequest_url()+httpRequestVo.getDevice_token());
			
		}else if(httpRequestVo.getDevice_type().equals("android")) {
			builder.url(httpRequestVo.getRequest_url());
		}
		Request request = null;
		Response res = null;		
		try {
			
			request = builder.build();
			res = client.newCall(request).execute();
			String result = "";
			if(res.code()!=200) {
				if(httpRequestVo.getDevice_type().equals("ios")) {
					result = ((JsonObject)new JsonParser()
							.parse(res.body().string()))
							.get("reason")
							.getAsString();
				}else if(httpRequestVo.getDevice_type().equals("android")) {
			//		logger.info("body : "+res.body().string());
					result = ((JsonObject) new JsonParser()
							.parse(res.body().string()))
							.getAsJsonObject("error")
							.get("message").getAsString();
				}
				return new HttpResponseVo(res.code(), result);
			}
		}catch(UnknownHostException e) {
			//네트워크 문제로 connection fail할 경우 10회 재 시도
			if(tryCount == 10) throw e;
        	tryCount++;
        	logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),tryCount);
        	Thread.sleep(1000);
        	client.newCall(request).cancel();
        	singlePushStart(httpRequestVo);
		}
		
		return new HttpResponseVo(res.code(), "");
		
	}
	
	public HttpResponseVo multiPushStart(HttpRequestVo httpRequestVo) throws Exception{
		
		//HTTP2 setting for use
		Security.insertProviderAt(Conscrypt.newProvider(), 1);
		
		//HttpClient setting
		OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.MINUTES)
					.retryOnConnectionFailure(true)
					.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
					.build();
		
		//Http header & body setting
		Builder builder = new Builder()
					.addHeader("Authorization", "Bearer " + httpRequestVo.getServer_token());
		//ios setting
		if(httpRequestVo.getDevice_type().equals("ios")) {
			builder.addHeader("apns-topic", httpRequestVo.getApns_topic())
					.url(httpRequestVo.getRequest_url()+httpRequestVo.getDevice_token());
		}
		int device_token_size = httpRequestVo.getDevice_token_list().size();
		cdl = new CountDownLatch(device_token_size);
		Request request = null;
		try {
				for(int i=0;i<device_token_size;i++) {
					request = builder.build();
					client.newCall(request).enqueue(this);
				}
				cdl.await();
				logger.info("PUSH SECCESS COUNT : "+success_count);
				logger.info("PUSH FAIL COUNT : "+fail_count);			
		}catch(Exception e) {
			new HttpResponseVo(500, e.getMessage());
		}
		
		return new HttpResponseVo(200, "");
		
	}
	
	@Override
	public void onResponse(Call call, Response response) throws IOException {
		// TODO Auto-generated method stub
		int response_code = response.code();
		String response_body = response.body().string();
		Buffer buffer = new Buffer();
		call.request().body().writeTo(buffer);
		logger.info(buffer.toString());
		if(response_code==200) {
			success_count++;
		}else {
			pushResponseVo.setCode(response_code); 
			pushResponseVo.setErrorMessage(response_body);
			fail_count++;
		}
		cdl.countDown();
		response.close();
	}
	
	@Override
	public void onFailure(Call call, IOException e){
		// TODO Auto-generated method stub
		try {
			fail_count++;
			cdl.countDown();
		//	String device_token=call.request().url().toString().split("/")[5];
			Buffer buffer = new Buffer();
			call.request().body().writeTo(buffer);
			logger.info(buffer.toString());
		}catch(IOException e1) {
			pushResponseVo.setCode(500); 
			pushResponseVo.setErrorMessage(e.getMessage());
		}
		
	}

	

	
	
}