package com.nexus.push.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.nexus.push.domain.HttpStatusDomain;
import com.nexus.push.domain.PushDomain;
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

	/*@Bean("ApnsFile")
	public File getApnsKeyFile() throws Exception{
		File getApnsKeyFile = new File(servletContext.getRealPath(env.getProperty("apns.keyPath")+"/"+env.getProperty("apns.p8.fileName")));
		return getApnsKeyFile;	
	}*/
	
	/*private File apnsKeyFile;
	
	@PostConstruct
	public void getApnsKeyFile() throws Exception{
		apnsKeyFile = new File(servletContext.getRealPath(env.getProperty("apns.keyPath")+"/"+env.getProperty("apns.p8.fileName")));
	}*/
	/*@Bean
	public PushDomain getApnsKeyFile() {
		PushDomain pushDomain = new PushDomain();
		pushDomain.setApns_keyFile(new File(servletContext.getRealPath(env.getProperty("apns.keyPath")+"/"+env.getProperty("apns.p8.fileName"))));
		return pushDomain;
	}*/
	HttpClient httpclient = new HttpClient();
	fcmApnsTokenHandler tokenHandler = new fcmApnsTokenHandler();
	
	@Override
	public HttpStatusDomain fcmPush(PushDomain pushDomain) throws Exception{
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
        FirebaseOptions options = new FirebaseOptions.Builder()
        		.setCredentials(GoogleCredentials.getApplicationDefault())
        		.build();
        FirebaseApp.initializeApp(options);
        String token1 = "dWVZuDKqAMk:APA91bH_sTG-7i699u9pO1Ti3qfnX_GrNPQ85XVujdmh3jdWkmlJtDGxjweT6env-lvSn9Jiqg5RAJw53x2Y7yhgeO9npmpBlK0HU8B9VnF4C8jk-UE4w6gHzx9D3B2mFPCrS4V4Hf3u";
		String token2 = "cizFiCvZXNE:APA91bFMWf_-r24BiaL1RSKf_4eFG7cP29dHcy0QP2BhjpIB-JeJbMy4U9tUbFEaUgiCsvW3HLkRtg03Xy8ILpVg_0WkW85Kf2Nfzsp2XXZ-ohGuvg8cT_J9SroZqEWSUHnG1GOLr1tX";
		List<String> registrationTokens = Arrays.asList(
				token1,token2
		);
		MulticastMessage message = MulticastMessage.builder()
				.putData("message", messageObject.toString())
				.addAllTokens(registrationTokens)
				.build();
		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
		logger.info("carrey : "+response.toString());
        return null;
	//	return httpclient.httpStart(pushDomain);
	}
	
	@Override
	public HttpStatusDomain apnsPush(PushDomain pushDomain) throws Exception{
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
        
		return httpclient.http2Start(pushDomain);
	}

	@Override
	public void fcmPushTest(PushDomain pushDomain) throws Exception {
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
        String keyRealPath = servletContext.getRealPath(pushDomain.getKey_path())+"/"+pushDomain.getKeyFile_name();

		FirebaseOptions options = new FirebaseOptions.Builder()
		  .setCredentials(GoogleCredentials.fromStream(new FileInputStream(keyRealPath)))
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
		String token2 = "c-9qEfth5GE:APA91bEmPuEtLMEuU8Z_cgpjDzONN9rWNvrCO3TXuN6MNhGsjVqtvBkS3UNwaSzg_Y-zASwaQysiTjG_lCe1MpdmTavwCH-FkKl0Llea1rSJtfOoWfhZeZoNtzYcKZeHOBF6znWM29rV";
		List<String> registrationTokens = Arrays.asList(
				token1,token2
		);
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
		}
		
	}
	
}
