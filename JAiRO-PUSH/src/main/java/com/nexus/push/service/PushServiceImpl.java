package com.nexus.push.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.nexus.push.domain.HttpResponseEntity;
import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.domain.HttpRequestVo;
import com.nexus.push.httpClient.HttpClient;
import com.nexus.push.util.HttpStatusCode;
import com.nexus.push.util.JWTHandler;
import com.nexus.push.util.JsonPostMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@PropertySource("classpath:/push.properties")
@Slf4j
public class PushServiceImpl implements PushService{

	@Autowired
	Environment env;
	@Autowired
	private HttpClient httpclient;
	@Autowired
	private JWTHandler tokenHandler;
	@Autowired
	private JsonPostMessageHandler jsonPostMessageHandler;
	
//	private static final String PUSH_SUCCESS = "PUSH SUCCESS";
//	private static final String PUSH_FAIL = "PUSH FAIL";
//	
//	private static final int STATUS_200_CODE = 200;
//	private static final int STATUS_400_CODE = 400;
//	private static final int STATUS_500_CODE = 500;
//	
//	public static final String CODE_400_DATA_ERROR = "Received data is null";
//	public static final String CODE_400_DEVICE_ERROR = "Device name is android or ios";
//	public static final String CODE_400_TOKEN_ERROR = "Token value is empty";
//	@Override
//	public HttpResponseVo fcmPush(HttpRequestVo httpRequestVo) throws Exception{
//		logger.info("FCM PUSH START !!!!!");
//		//fcm token setting
//		httpRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
//		httpRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
//		tokenHandler.fcmTokenSet(httpRequestVo);
//		
//		//fcm POST data setting
//		logger.info("FCM PUSH DATA MAKE START !!!!!");
//        JSONObject messageObject = new JSONObject();
//        JSONObject dataObject = new JSONObject();
//        JSONObject androidObject = new JSONObject();
//        JSONObject parentObject = new JSONObject();
//        androidObject.put("priority", "high");
//        dataObject.put("menu", httpRequestVo.getMenu());
//        dataObject.put("id", httpRequestVo.getId());
//        dataObject.put("group_id", httpRequestVo.getGroup_id());
//        dataObject.put("mode", httpRequestVo.getMode());
//        dataObject.put("title", httpRequestVo.getTitle());
//        dataObject.put("from_id", httpRequestVo.getFrom());
//        dataObject.put("message", httpRequestVo.getMessage());
//        messageObject.put("token", httpRequestVo.getDevice_token()); 
//        //messageObject.put("notification", dataObject);
//        messageObject.put("android", androidObject);
//        messageObject.put("data", dataObject);
//        parentObject.put("message", messageObject); // deviceID
//        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
//        httpRequestVo.setPost_data(parentObject.toString());
//        logger.info("FCM PUSH DATA MAKE END !!!!!");
//        
//        //fcm url setting
//        httpRequestVo.setFcm_start_url(env.getProperty("fcm.start.url"));
//        httpRequestVo.setFcm_end_url(env.getProperty("fcm.end.url"));
//        httpRequestVo.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
//        httpRequestVo.setRequest_type(env.getProperty("fcm.req.type"));
//        httpRequestVo.setFcm_full_url(
//        						httpRequestVo.getRequest_type()+
//        						httpRequestVo.getFcm_start_url()+
//        						httpRequestVo.getFcm_end_url()
//        					  );
//       /* Map<String,String> map = null;
//        try {
//        	map = new ObjectMapper().readValue(parentObject.toString(), Map.class);
//        }catch(Exception e) {
//        	e.printStackTrace();
//        }*/
////        FirebaseOptions options = new FirebaseOptions.Builder()
////        		.setCredentials(GoogleCredentials.getApplicationDefault())
////        		.build();
////        FirebaseApp.initializeApp(options);
////        String token1 = "dWVZuDKqAMk:APA91bH_sTG-7i699u9pO1Ti3qfnX_GrNPQ85XVujdmh3jdWkmlJtDGxjweT6env-lvSn9Jiqg5RAJw53x2Y7yhgeO9npmpBlK0HU8B9VnF4C8jk-UE4w6gHzx9D3B2mFPCrS4V4Hf3u";
////		String token2 = "cizFiCvZXNE:APA91bFMWf_-r24BiaL1RSKf_4eFG7cP29dHcy0QP2BhjpIB-JeJbMy4U9tUbFEaUgiCsvW3HLkRtg03Xy8ILpVg_0WkW85Kf2Nfzsp2XXZ-ohGuvg8cT_J9SroZqEWSUHnG1GOLr1tX";
////		List<String> registrationTokens = Arrays.asList(
////				token1,token2
////		);
////		MulticastMessage message = MulticastMessage.builder()
////				.putData("message", messageObject.toString())
////				.addAllTokens(registrationTokens)
////				.build();
////		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
////		logger.info("carrey : "+response.toString());
////        return null;
////		return httpclient.httpStart(httpRequestVo);
//        httpclient.httpMultiStart(httpRequestVo);
//        return null;
//	}
//	
//	@Override
//	public HttpResponseVo apnsPush(HttpRequestVo httpRequestVo) throws Exception{
//		logger.info("APNS PUSH START !!!!!");
//		//apns token setting
//		httpRequestVo.setKey_id(env.getProperty("apns.keyId"));
//		httpRequestVo.setTeam_id(env.getProperty("apns.teamId"));
//		httpRequestVo.setKey_path(env.getProperty("apns.keyPath"));
//		httpRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
//	//	httpRequestVo.setApns_keyFile(apnsKeyFile);
//		tokenHandler.apnsTokenSet(httpRequestVo);
//		
//		//apns POST data setting
//		logger.info("APNS PUSH DATA MAKE START !!!!!");
//		JSONObject apsObject = new JSONObject();
//		//JSONObject alertObject = new JSONObject();
//		JSONObject dataObject = new JSONObject();
//		dataObject.put("menu", httpRequestVo.getMenu());
//		dataObject.put("id", httpRequestVo.getId());
//		dataObject.put("group_id", httpRequestVo.getGroup_id());
//		dataObject.put("mode", httpRequestVo.getMode());
//		dataObject.put("title", httpRequestVo.getTitle());
//		dataObject.put("from_id", httpRequestVo.getFrom());
//		dataObject.put("message", httpRequestVo.getMessage());
//		//alertObject.put("alert", dataObject);
//		apsObject.put("aps", dataObject);
//		logger.info("APNS PUSH DATA : {}",apsObject.toString());
//        httpRequestVo.setPost_data(apsObject.toString());
//        logger.info("APNS PUSH DATA MAKE END !!!!!");
//        
//        //apns topic setting
//        httpRequestVo.setApns_topic(env.getProperty("apns.topic"));
//        
//        //apns url setting
//        httpRequestVo.setApns_start_url(env.getProperty("apns.start.url"));
//        httpRequestVo.setApns_end_url(env.getProperty("apns.end.url"));
//        httpRequestVo.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
//        httpRequestVo.setRequest_type(env.getProperty("apns.req.type"));
//        
//        return null;
//	//	return httpclient.http2Start(httpRequestVo);
//	}
//	
//	@Override
//	public void apnsMultiPushTest(HttpRequestVo httpRequestVo) throws Exception{
//		logger.info("APNS PUSH START !!!!!");
//		//apns token setting
//		httpRequestVo.setKey_id(env.getProperty("apns.keyId"));
//		httpRequestVo.setTeam_id(env.getProperty("apns.teamId"));
//		httpRequestVo.setKey_path(env.getProperty("apns.keyPath"));
//		httpRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
//	//	httpRequestVo.setApns_keyFile(apnsKeyFile);
//		tokenHandler.apnsTokenSet(httpRequestVo);
//		
//		//apns POST data setting
//		logger.info("APNS PUSH DATA MAKE START !!!!!");
//		JSONObject apsObject = new JSONObject();
//		JSONObject dataObject = new JSONObject();
//		dataObject.put("menu", httpRequestVo.getMenu());
//		dataObject.put("id", httpRequestVo.getId());
//		dataObject.put("group_id", httpRequestVo.getGroup_id());
//		dataObject.put("mode", httpRequestVo.getMode());
//		dataObject.put("title", httpRequestVo.getTitle());
//		dataObject.put("from_id", httpRequestVo.getFrom());
//		dataObject.put("message", httpRequestVo.getMessage());
//		apsObject.put("aps", dataObject);
//		logger.info("APNS PUSH DATA : {}",apsObject.toString());
//        httpRequestVo.setPost_data(apsObject.toString());
//        logger.info("APNS PUSH DATA MAKE END !!!!!");
//        
//        //apns topic setting
//        httpRequestVo.setApns_topic(env.getProperty("apns.topic"));
//        
//        //apns url setting
//        httpRequestVo.setApns_start_url(env.getProperty("apns.start.url"));
//        httpRequestVo.setApns_end_url(env.getProperty("apns.end.url"));
//        httpRequestVo.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
//        httpRequestVo.setRequest_type(env.getProperty("apns.req.type"));
//        
//		httpclient.http2MultiStart(httpRequestVo);
//	}
//
//	@Override
//	public void fcmMultiPushTest(HttpRequestVo httpRequestVo) throws Exception {
//		// TODO Auto-generated method stub
//		logger.info("FCM PUSH START !!!!!");
//		//fcm token setting
//		httpRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
//		httpRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
//		tokenHandler.fcmTokenSet(httpRequestVo);
//		
//		//fcm POST data setting
//		logger.info("FCM PUSH DATA MAKE START !!!!!");
//        JSONObject messageObject = new JSONObject();
//        JSONObject dataObject = new JSONObject();
//        JSONObject androidObject = new JSONObject();
//        JSONObject parentObject = new JSONObject();
//        androidObject.put("priority", "high");
//        dataObject.put("menu", httpRequestVo.getMenu());
//        dataObject.put("id", httpRequestVo.getId());
//        dataObject.put("group_id", httpRequestVo.getGroup_id());
//        dataObject.put("mode", httpRequestVo.getMode());
//        dataObject.put("title", httpRequestVo.getTitle());
//        dataObject.put("from_id", httpRequestVo.getFrom());
//        dataObject.put("message", httpRequestVo.getMessage());
//        messageObject.put("token", httpRequestVo.getDevice_token()); 
//        //messageObject.put("notification", dataObject);
//        messageObject.put("android", androidObject);
//        messageObject.put("data", dataObject);
//        parentObject.put("message", messageObject); // deviceID
//        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
//        httpRequestVo.setPost_data(parentObject.toString());
//        logger.info("FCM PUSH DATA MAKE END !!!!!");
//        
//        //fcm url setting
//        httpRequestVo.setFcm_start_url(env.getProperty("fcm.start.url"));
//        httpRequestVo.setFcm_end_url(env.getProperty("fcm.end.url"));
//        httpRequestVo.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
//        httpRequestVo.setRequest_type(env.getProperty("fcm.req.type"));
//        httpRequestVo.setFcm_full_url(
//        						httpRequestVo.getRequest_type()+
//        						httpRequestVo.getFcm_start_url()+
//        						httpRequestVo.getFcm_end_url()
//        					  );
//		FirebaseOptions options = new FirebaseOptions.Builder()
//		  .setCredentials(GoogleCredentials.fromStream(new FileInputStream(new ClassPathResource(httpRequestVo.getKeyFile_name()).getFile())))
//		  .setDatabaseUrl("https://waiv-a098f.firebaseio.com")
//		  .build();
//		logger.info("옵션!!");
//        FirebaseApp firebaseApp = null;
//        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
//        if(firebaseApps !=null && !firebaseApps.isEmpty()) {
//        	logger.info("이미 있는 앱");
//        	for(FirebaseApp app : firebaseApps) {
//        		if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))
//        			firebaseApp = app;
//        	}
//        }else {
//        	firebaseApp = FirebaseApp.initializeApp(options);
//        }
//        
//		String token1 = "dWVZuDKqAMk:APA91bH_sTG-7i699u9pO1Ti3qfnX_GrNPQ85XVujdmh3jdWkmlJtDGxjweT6env-lvSn9Jiqg5RAJw53x2Y7yhgeO9npmpBlK0HU8B9VnF4C8jk-UE4w6gHzx9D3B2mFPCrS4V4Hf3u";
//		String token2 = "c-9qEfth5GE:APA91bEmPuEtLMEuU8Z_cgpjDzONN9rWNvrCO3TXuN6MNhGsjVqtvBkS3UNwaSzg_Y-zASwaQysiTjG_lCe1MpdmTavwCH-FkKl0Llea1rSJtfOoWfhZeZoNtzYcKZeHOBF6znWM29r";
//		List<String> registrationTokens = new ArrayList<String>();
//		for(int i=0;i<50;i++) {
//			registrationTokens.add(token1);
//			registrationTokens.add(token2);
//		}
//		
//		MulticastMessage message = MulticastMessage.builder()
//				.putData("message", messageObject.toString())
//				.addAllTokens(registrationTokens)
//				.build();
//		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
//		firebaseApp.delete();
//		logger.info("성공 개수 : "+response.getSuccessCount());
//		logger.info("실패 개수 : "+response.getFailureCount());
//		for(int i=0;i<response.getResponses().size();i++) {
//			logger.info("messageID : "+response.getResponses().get(i).getMessageId());
//			logger.info("Exception : "+response.getResponses().get(i).getException());
//			logger.info(""+response.getResponses());
//		}
//		
//	}

	@Override
	public ResponseEntity<HttpResponseVo> waivPush(HttpRequestVo httpRequestVo) {
		logger.info("PUSH EXECUTE!!!");
		//HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
		//HttpResponseVo hrv= new HttpResponseVo();
		try{
			/* Basic Data Check */
			BodyBuilder responseEntity = ResponseEntity.status(HttpStatusCode.STATUS_400_CODE);
			HttpResponseVo hrv = new HttpResponseVo(HttpStatusCode.STATUS_400_CODE, HttpStatusCode.PUSH_FAIL, HttpStatusCode.CODE_400_DEVICE_ERROR);
			//NO DATA
			if(httpRequestVo==null) {
				logger.info("PUSH FAIL 400 error : "+HttpStatusCode.CODE_400_DATA_ERROR);
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, CODE_400_DATA_ERROR);
				hrv.setErrorMessage(HttpStatusCode.CODE_400_DATA_ERROR);
				return responseEntity.body(hrv);
			}
			//NO DEVICE
			else if(httpRequestVo.getDevice_type()==null || httpRequestVo.getDevice_type().equals("")){
				logger.info("PUSH FAIL 400 error : "+HttpStatusCode.CODE_400_DEVICE_ERROR);
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, CODE_400_DEVICE_ERROR);
				return responseEntity.body(hrv);
			}else if(!(httpRequestVo.getDevice_type().equals("ios") || httpRequestVo.getDevice_type().equals("android"))){
				logger.info("PUSH FAIL 400 error : "+HttpStatusCode.CODE_400_DEVICE_ERROR);
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, CODE_400_DEVICE_ERROR);
				return responseEntity.body(hrv);
			}
			//NO DEVICE TOKEN
			else if(httpRequestVo.getDevice_token()==null || httpRequestVo.getDevice_token().equals("")) {
				logger.info("PUSH FAIL 400 error : "+HttpStatusCode.CODE_400_TOKEN_ERROR);
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, CODE_400_TOKEN_ERROR);
				return responseEntity.body(hrv);
			}
			
			/* Push Start */
			switch(httpRequestVo.getDevice_type()) {
				case "ios" : 
					//JWT Setting
					httpRequestVo.setKey_id(env.getProperty("apns.keyId"));
					httpRequestVo.setTeam_id(env.getProperty("apns.teamId"));
					httpRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
					httpRequestVo.setApns_topic(env.getProperty("apns.topic"));
					tokenHandler.apnsTokenSet(httpRequestVo);
					
					//POST Message Setting
					jsonPostMessageHandler.waivPostMessageSet(httpRequestVo);
					
					//Request Url Setting
					httpRequestVo.setRequest_url(env.getProperty("apns.url"));
					
					//HttpClient Start
					hrv = httpclient.singlePushStart(httpRequestVo);
					break;
				case "android" : 
					//JWT Setting
					httpRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
					httpRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
					tokenHandler.fcmTokenSet(httpRequestVo);
					
					//Post Message Setting
					jsonPostMessageHandler.waivPostMessageSet(httpRequestVo);
					
					//Request Url Setting
					httpRequestVo.setRequest_url(env.getProperty("fcm.url"));
					
					//HttpClient Start
					hrv = httpclient.singlePushStart(httpRequestVo);
					break;
			}
			int result_code = hrv.getCode();
			String error_message = hrv.getErrorMessage();
			
			//status code가 200으로 return될 경우 PUSH SUCCESS
			if(result_code==200) {
				logger.info("PUSH SUCCESS");
				//return httpResponseEntity.httpResponse(PUSH_SUCCESS, STATUS_200_CODE, "");
				return ResponseEntity.ok(hrv);
			//잘못된 디바이스 토큰이라는 메세지가 리턴된 경우 400 request error
			}else if(error_message.equals("BadDeviceToken")){
				logger.info("PUSH FAIL 400 error : "+hrv.getErrorMessage());
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, httpResponseVo.getErrorMessage());
				return ResponseEntity.status(HttpStatusCode.STATUS_400_CODE).body(hrv);
			}else if(error_message.equals("The registration token is not a valid FCM registration token")){
				logger.info("PUSH FAIL 400 error : "+hrv.getErrorMessage());
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_400_CODE, httpResponseVo.getErrorMessage());
				return ResponseEntity.status(HttpStatusCode.STATUS_400_CODE).body(hrv);
			//그외의 케이스인 경우 500 interval server error
			}else{
				logger.info("PUSH FAIL 500 error : "+hrv.getErrorMessage());
				//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_500_CODE, hrv.getErrorMessage());
				return ResponseEntity.status(HttpStatusCode.STATUS_500_CODE).body(hrv);
			}
		}catch(Exception e) {
			
			//예외 발생 시 500 interval server error
			logger.info("PUSH FAIL 500 error : "+e.toString());
			//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_500_CODE, e.toString());
			return ResponseEntity.status(HttpStatusCode.STATUS_500_CODE).body(new HttpResponseVo(HttpStatusCode.PUSH_FAIL, e.toString()));
		}
	}

	@Override
	public ResponseEntity<HttpResponseVo> visitKoreaPush(HttpRequestVo httpRequestVo) {
		// TODO Auto-generated method stub
		
		//HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
		HttpResponseVo hrv = new HttpResponseVo();
		try {
			switch(httpRequestVo.getDevice_type()) {
			case "ios" : 
				//JWT Setting
				httpRequestVo.setKey_id(env.getProperty("apns.keyId"));
				httpRequestVo.setTeam_id(env.getProperty("apns.teamId"));
				httpRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
				httpRequestVo.setApns_topic(env.getProperty("apns.topic"));
				tokenHandler.apnsTokenSet(httpRequestVo);
				
				//POST Message Setting
				jsonPostMessageHandler.waivPostMessageSet(httpRequestVo);
				
				//Request Url Setting
				httpRequestVo.setRequest_url(env.getProperty("apns.url"));
				
				//HttpClient Start
				hrv = httpclient.multiPushStart(httpRequestVo);
				break;
			case "android" : 
				//JWT Setting
				httpRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
				httpRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
				tokenHandler.fcmTokenSet(httpRequestVo);
				
				//Post Message Setting
				jsonPostMessageHandler.waivPostMessageSet(httpRequestVo);
				
				//Request Url Setting
				httpRequestVo.setRequest_url(env.getProperty("fcm.url"));
				
				//HttpClient Start
				hrv = httpclient.multiPushStart(httpRequestVo);
				break;
		}
			if(hrv.getCode()!=200) {
				//return httpResponseEntity.httpResponse(PUSH_FAIL, httpResponseVo.getCode(), httpResponseVo.getErrorMessage());
				return ResponseEntity.status(hrv.getCode()).body(hrv);
			}
		}catch(Exception e) {
			//return httpResponseEntity.httpResponse(PUSH_FAIL, STATUS_500_CODE, e.toString());
			return ResponseEntity.status(HttpStatusCode.STATUS_500_CODE).body(hrv);
		}
		
		//return httpResponseEntity.httpResponse(PUSH_SUCCESS, STATUS_200_CODE, "");
		return ResponseEntity.ok(hrv);
	}
	
}
