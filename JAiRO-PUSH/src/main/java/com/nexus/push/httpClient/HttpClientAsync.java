package com.nexus.push.httpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexus.push.domain.PushDomain;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

@Slf4j
public class HttpClientAsync {
	
	@Async
	public void httpClientAsync(OkHttpClient client, PushDomain pushDomain, int index, CountDownLatch cdl, Map<String,Integer> map) throws Exception{
		logger.info("carrey : "+index);
		
		Request request = null;
		
		
			request = new Request.Builder()
					.addHeader("Authorization", "Bearer " + pushDomain.getServer_token())
	        		.addHeader("Content-Type", "application/json; UTF-8")
	        		.url(pushDomain.getFcm_full_url())
	        		.post(RequestBody.create(MediaType.parse("application/json"), pushDomain.getPost_data()))
	        		.build();
			client.newCall(request).enqueue(new Callback() {
				
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					// TODO Auto-generated method stub
					
					Buffer buffer = new Buffer();
					call.request().body().writeTo(buffer);
				//	logger.info("성공 : "+buffer.readUtf8());
					map.put("success", map.get("success")+1);
					response.close();
					cdl.countDown();
				}
				
				@Override
				public void onFailure(Call call, IOException e){
					// TODO Auto-generated method stub
					Buffer buffer = new Buffer();
					try {
						call.request().body().writeTo(buffer);
					}catch(IOException e1) {
						e1.printStackTrace();
					}
					
				//	logger.info("실패 : "+buffer.readUtf8());
					map.put("fail", map.get("fail")+1);
					cdl.countDown();
				}
			});
	}
}
