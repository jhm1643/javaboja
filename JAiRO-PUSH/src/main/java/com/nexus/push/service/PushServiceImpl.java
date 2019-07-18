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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.nexus.push.domain.ErrorCode;
import com.nexus.push.domain.HttpResponseEntity;
import com.nexus.push.domain.PushResult;
import com.nexus.push.domain.PushRequestObject;
import com.nexus.push.httpClient.HttpClient;
import com.nexus.push.util.fcmApnsTokenHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@PropertySource("classpath:/push.properties")
@Slf4j
public class PushServiceImpl implements PushService{

	@Autowired
	Environment env;

	@Autowired
	ServletContext servletContext;

	@Autowired
	private HttpClient httpclient;

	fcmApnsTokenHandler tokenHandler = new fcmApnsTokenHandler();
	
	@Override
	public PushResult fcmPush(PushRequestObject pushDomain) throws Exception{
		logger.info("FCM PUSH START !!!!!");
		//fcm token setting
		pushDomain.setKey_path(env.getProperty("fcm.keyPath"));
		pushDomain.setKeyFile_name(env.getProperty("fcm.key.fileName"));
		tokenHandler.fcmTokenSet(pushDomain, servletContext);
		
		//fcm POST data setting
		logger.info("FCM PUSH DATA MAKE START !!!!!");
        JSONObject messageObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        JSONObject androidObject = new JSONObject();
        JSONObject parentObject = new JSONObject();
        androidObject.put("priority", "high");
        dataObject.put("menu", pushDomain.getMenu());
        dataObject.put("id", pushDomain.getId());
        dataObject.put("group_id", pushDomain.getGroup_id());
        dataObject.put("mode", pushDomain.getMode());
        dataObject.put("title", pushDomain.getTitle());
        dataObject.put("from_id", pushDomain.getFrom());
        dataObject.put("message", pushDomain.getMessage());
        messageObject.put("token", pushDomain.getDevice_token()); 
        //messageObject.put("notification", dataObject);
        messageObject.put("android", androidObject);
        messageObject.put("data", dataObject);
        parentObject.put("message", messageObject); // deviceID
        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
        pushDomain.setPost_data(parentObject.toString());
        logger.info("FCM PUSH DATA MAKE END !!!!!");
        
        //fcm url setting
        pushDomain.setFcm_start_url(env.getProperty("fcm.start.url"));
        pushDomain.setFcm_end_url(env.getProperty("fcm.end.url"));
        pushDomain.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
        pushDomain.setRequest_type(env.getProperty("fcm.req.type"));
        pushDomain.setFcm_full_url(
        						pushDomain.getRequest_type()+
        						pushDomain.getFcm_start_url()+
        						pushDomain.getFcm_end_url()
        					  );
       /* Map<String,String> map = null;
        try {
        	map = new ObjectMapper().readValue(parentObject.toString(), Map.class);
        }catch(Exception e) {
        	e.printStackTrace();
        }*/
//        FirebaseOptions options = new FirebaseOptions.Builder()
//        		.setCredentials(GoogleCredentials.getApplicationDefault())
//        		.build();
//        FirebaseApp.initializeApp(options);
//        String token1 = "dWVZuDKqAMk:APA91bH_sTG-7i699u9pO1Ti3qfnX_GrNPQ85XVujdmh3jdWkmlJtDGxjweT6env-lvSn9Jiqg5RAJw53x2Y7yhgeO9npmpBlK0HU8B9VnF4C8jk-UE4w6gHzx9D3B2mFPCrS4V4Hf3u";
//		String token2 = "cizFiCvZXNE:APA91bFMWf_-r24BiaL1RSKf_4eFG7cP29dHcy0QP2BhjpIB-JeJbMy4U9tUbFEaUgiCsvW3HLkRtg03Xy8ILpVg_0WkW85Kf2Nfzsp2XXZ-ohGuvg8cT_J9SroZqEWSUHnG1GOLr1tX";
//		List<String> registrationTokens = Arrays.asList(
//				token1,token2
//		);
//		MulticastMessage message = MulticastMessage.builder()
//				.putData("message", messageObject.toString())
//				.addAllTokens(registrationTokens)
//				.build();
//		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
//		logger.info("carrey : "+response.toString());
//        return null;
//		return httpclient.httpStart(pushDomain);
        httpclient.httpMultiStart(pushDomain);
        return null;
	}
	
	@Override
	public PushResult apnsPush(PushRequestObject pushDomain) throws Exception{
		logger.info("APNS PUSH START !!!!!");
		//apns token setting
		pushDomain.setKey_id(env.getProperty("apns.keyId"));
		pushDomain.setTeam_id(env.getProperty("apns.teamId"));
		pushDomain.setKey_path(env.getProperty("apns.keyPath"));
		pushDomain.setKeyFile_name(env.getProperty("apns.p8.fileName"));
	//	pushDomain.setApns_keyFile(apnsKeyFile);
		tokenHandler.apnsTokenSet(pushDomain, servletContext);
		
		//apns POST data setting
		logger.info("APNS PUSH DATA MAKE START !!!!!");
		JSONObject apsObject = new JSONObject();
		//JSONObject alertObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		dataObject.put("menu", pushDomain.getMenu());
		dataObject.put("id", pushDomain.getId());
		dataObject.put("group_id", pushDomain.getGroup_id());
		dataObject.put("mode", pushDomain.getMode());
		dataObject.put("title", pushDomain.getTitle());
		dataObject.put("from_id", pushDomain.getFrom());
		dataObject.put("message", pushDomain.getMessage());
		//alertObject.put("alert", dataObject);
		apsObject.put("aps", dataObject);
		logger.info("APNS PUSH DATA : {}",apsObject.toString());
        pushDomain.setPost_data(apsObject.toString());
        logger.info("APNS PUSH DATA MAKE END !!!!!");
        
        //apns topic setting
        pushDomain.setApns_topic(env.getProperty("apns.topic"));
        
        //apns url setting
        pushDomain.setApns_start_url(env.getProperty("apns.start.url"));
        pushDomain.setApns_end_url(env.getProperty("apns.end.url"));
        pushDomain.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
        pushDomain.setRequest_type(env.getProperty("apns.req.type"));
        
        return null;
	//	return httpclient.http2Start(pushDomain);
	}
	
	@Override
	public void apnsMultiPushTest(PushRequestObject pushDomain) throws Exception{
		logger.info("APNS PUSH START !!!!!");
		//apns token setting
		pushDomain.setKey_id(env.getProperty("apns.keyId"));
		pushDomain.setTeam_id(env.getProperty("apns.teamId"));
		pushDomain.setKey_path(env.getProperty("apns.keyPath"));
		pushDomain.setKeyFile_name(env.getProperty("apns.p8.fileName"));
	//	pushDomain.setApns_keyFile(apnsKeyFile);
		tokenHandler.apnsTokenSet(pushDomain, servletContext);
		
		//apns POST data setting
		logger.info("APNS PUSH DATA MAKE START !!!!!");
		JSONObject apsObject = new JSONObject();
		//JSONObject alertObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		dataObject.put("menu", pushDomain.getMenu());
		dataObject.put("id", pushDomain.getId());
		dataObject.put("group_id", pushDomain.getGroup_id());
		dataObject.put("mode", pushDomain.getMode());
		dataObject.put("title", pushDomain.getTitle());
		dataObject.put("from_id", pushDomain.getFrom());
		dataObject.put("message", pushDomain.getMessage());
		//alertObject.put("alert", dataObject);
		apsObject.put("aps", dataObject);
		logger.info("APNS PUSH DATA : {}",apsObject.toString());
        pushDomain.setPost_data(apsObject.toString());
        logger.info("APNS PUSH DATA MAKE END !!!!!");
        
        //apns topic setting
        pushDomain.setApns_topic(env.getProperty("apns.topic"));
        
        //apns url setting
        pushDomain.setApns_start_url(env.getProperty("apns.start.url"));
        pushDomain.setApns_end_url(env.getProperty("apns.end.url"));
        pushDomain.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
        pushDomain.setRequest_type(env.getProperty("apns.req.type"));
        
		httpclient.http2MultiStart(pushDomain);
	}

	@Override
	public void fcmMultiPushTest(PushRequestObject pushDomain) throws Exception {
		// TODO Auto-generated method stub
		logger.info("FCM PUSH START !!!!!");
		//fcm token setting
		pushDomain.setKey_path(env.getProperty("fcm.keyPath"));
		pushDomain.setKeyFile_name(env.getProperty("fcm.key.fileName"));
		tokenHandler.fcmTokenSet(pushDomain, servletContext);
		
		//fcm POST data setting
		logger.info("FCM PUSH DATA MAKE START !!!!!");
        JSONObject messageObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        JSONObject androidObject = new JSONObject();
        JSONObject parentObject = new JSONObject();
        androidObject.put("priority", "high");
        dataObject.put("menu", pushDomain.getMenu());
        dataObject.put("id", pushDomain.getId());
        dataObject.put("group_id", pushDomain.getGroup_id());
        dataObject.put("mode", pushDomain.getMode());
        dataObject.put("title", pushDomain.getTitle());
        dataObject.put("from_id", pushDomain.getFrom());
        dataObject.put("message", pushDomain.getMessage());
        messageObject.put("token", pushDomain.getDevice_token()); 
        //messageObject.put("notification", dataObject);
        messageObject.put("android", androidObject);
        messageObject.put("data", dataObject);
        parentObject.put("message", messageObject); // deviceID
        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
        pushDomain.setPost_data(parentObject.toString());
        logger.info("FCM PUSH DATA MAKE END !!!!!");
        
        //fcm url setting
        pushDomain.setFcm_start_url(env.getProperty("fcm.start.url"));
        pushDomain.setFcm_end_url(env.getProperty("fcm.end.url"));
        pushDomain.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
        pushDomain.setRequest_type(env.getProperty("fcm.req.type"));
        pushDomain.setFcm_full_url(
        						pushDomain.getRequest_type()+
        						pushDomain.getFcm_start_url()+
        						pushDomain.getFcm_end_url()
        					  );
		FirebaseOptions options = new FirebaseOptions.Builder()
		  .setCredentials(GoogleCredentials.fromStream(new FileInputStream(new ClassPathResource(pushDomain.getKeyFile_name()).getFile())))
		  .setDatabaseUrl("https://waiv-a098f.firebaseio.com")
		  .build();
		logger.info("옵션!!");
        FirebaseApp firebaseApp = null;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if(firebaseApps !=null && !firebaseApps.isEmpty()) {
        	logger.info("이미 있는 앱");
        	for(FirebaseApp app : firebaseApps) {
        		if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))
        			firebaseApp = app;
        	}
        }else {
        	firebaseApp = FirebaseApp.initializeApp(options);
        }
        
		String token1 = "dWVZuDKqAMk:APA91bH_sTG-7i699u9pO1Ti3qfnX_GrNPQ85XVujdmh3jdWkmlJtDGxjweT6env-lvSn9Jiqg5RAJw53x2Y7yhgeO9npmpBlK0HU8B9VnF4C8jk-UE4w6gHzx9D3B2mFPCrS4V4Hf3u";
		String token2 = "c-9qEfth5GE:APA91bEmPuEtLMEuU8Z_cgpjDzONN9rWNvrCO3TXuN6MNhGsjVqtvBkS3UNwaSzg_Y-zASwaQysiTjG_lCe1MpdmTavwCH-FkKl0Llea1rSJtfOoWfhZeZoNtzYcKZeHOBF6znWM29r";
		List<String> registrationTokens = new ArrayList<String>();
		for(int i=0;i<50;i++) {
			registrationTokens.add(token1);
			registrationTokens.add(token2);
		}
		
		MulticastMessage message = MulticastMessage.builder()
				.putData("message", messageObject.toString())
				.addAllTokens(registrationTokens)
				.build();
		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
		firebaseApp.delete();
		logger.info("성공 개수 : "+response.getSuccessCount());
		logger.info("실패 개수 : "+response.getFailureCount());
		for(int i=0;i<response.getResponses().size();i++) {
			logger.info("messageID : "+response.getResponses().get(i).getMessageId());
			logger.info("Exception : "+response.getResponses().get(i).getException());
			logger.info(""+response.getResponses());
		}
		
	}

	@Override
	public ResponseEntity<PushResult> push(PushRequestObject pushDomain) {
		logger.info("PUSH EXECUTE!!!");
		HttpResponseEntity resResult = new HttpResponseEntity();
		PushResult httpStatusDomain= new PushResult();
		try{
			
			/* Basic Data Check */
			//NO DATA
			if(pushDomain==null) {
				logger.info("PUSH FAIL 400 error : "+ErrorCode.CODE_400_DATA_ERROR);
				return resResult.httpResponse("PUSH FAIL",400, ErrorCode.CODE_400_DATA_ERROR);
			}
			//NO DEVICE
			else if(pushDomain.getDevice()==null || pushDomain.getDevice().equals("")){
				logger.info("PUSH FAIL 400 error : "+ErrorCode.CODE_400_DEVICE_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,ErrorCode.CODE_400_DEVICE_ERROR);
			}else if(!(pushDomain.getDevice().equals("ios") || pushDomain.getDevice().equals("android"))){
				logger.info("PUSH FAIL 400 error : "+ErrorCode.CODE_400_DEVICE_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,ErrorCode.CODE_400_DEVICE_ERROR);
			}
			//NO DEVICE TOKEN
			else if(pushDomain.getDevice_token()==null || pushDomain.getDevice_token().equals("")) {
				logger.info("PUSH FAIL 400 error : "+ErrorCode.CODE_400_TOKEN_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,ErrorCode.CODE_400_TOKEN_ERROR);
			}
			
			/* Push Start */
			switch(pushDomain.getDevice()) {
				case "ios" : 
					//httpStatusDomain=pushService.apnsPush(pushDomain);
					break;
				case "android" : 
				//	httpStatusDomain=pushService.fcmPush(pushDomain);
					break;
			}
			int result_code = httpStatusDomain.getCode();
			String error_message = httpStatusDomain.getErrorMessage();
			
			//status code가 200으로 return될 경우 PUSH SUCCESS
			if(result_code==200) {
				logger.info("PUSH SUCCESS");
				return resResult.httpResponse("PUSH SUCCESS",200,"");
			
			//잘못된 디바이스 토큰이라는 메세지가 리턴된 경우 400 request error
			}else if(error_message.equals("BadDeviceToken")){
				logger.info("PUSH FAIL 400 error : "+httpStatusDomain.getErrorMessage());
				return resResult.httpResponse("PUSH FAIL",400,httpStatusDomain.getErrorMessage());
			}else if(error_message.equals("The registration token is not a valid FCM registration token")){
				logger.info("PUSH FAIL 400 error : "+httpStatusDomain.getErrorMessage());
				return resResult.httpResponse("PUSH FAIL",400,httpStatusDomain.getErrorMessage());
			
			//그외의 케이스인 경우 500 interval server error
			}else{
				logger.info("PUSH FAIL 500 error : "+httpStatusDomain.getErrorMessage());
				return resResult.httpResponse("PUSH FAIL",500,httpStatusDomain.getErrorMessage());
			}
		}catch(Exception e) {
			
			//예외 발생 시 500 interval server error
			logger.info("PUSH FAIL 500 error : "+e.toString());
			return resResult.httpResponse("PUSH FAIL",500,e.toString());
		}
	}
	
}
