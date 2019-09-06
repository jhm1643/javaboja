package com.nexus.push.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import com.nexus.push.domain.PushResponseVo;
import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushMember;
import com.nexus.push.dao.PushContentDao;
import com.nexus.push.dao.PushMemberDao;
import com.nexus.push.domain.PushRequestVo;
import com.nexus.push.httpClient.HttpClient;
import com.nexus.push.util.HttpStatusCode;
import com.nexus.push.util.JWTHandler;
import com.nexus.push.util.JsonPostMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@PropertySource("classpath:/push.properties")
@Slf4j
public class MobilePushServiceImpl extends HttpStatusCode implements MobilePushService{

	@Autowired
	Environment env;
	@Autowired
	private HttpClient httpclient;
	@Autowired
	private JWTHandler tokenHandler;
	@Autowired
	private JsonPostMessageHandler jsonPostMessageHandler;
	@Autowired
	private PushContentDao pushContentDao;
	@Autowired
	private PushMemberDao pushMemberDao;
	@Override
	public ResponseEntity<PushResponseVo> waivPush(PushRequestVo pushRequestVo) {
		logger.info("PUSH EXECUTE!!!");
		//HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
		//HttpResponseVo hrv= new HttpResponseVo();
		try{
			/* Basic Data Check */
			BodyBuilder responseEntity = ResponseEntity.status(STATUS_400_CODE);
			PushResponseVo hrv = new PushResponseVo(STATUS_400_CODE, PUSH_FAIL, CODE_400_DEVICE_ERROR);
			//NO DATA
			if(pushRequestVo==null) {
				logger.info("PUSH FAIL 400 error : "+CODE_400_DATA_ERROR);
				hrv.setErrorMessage(CODE_400_DATA_ERROR);
				return responseEntity.body(hrv);
			}
			//NO DEVICE
			else if(pushRequestVo.getDevice_type()==null || pushRequestVo.getDevice_type().equals("")){
				logger.info("PUSH FAIL 400 error : "+CODE_400_DEVICE_ERROR);
				return responseEntity.body(hrv);
			}else if(!(pushRequestVo.getDevice_type().equals("ios") || pushRequestVo.getDevice_type().equals("android"))){
				logger.info("PUSH FAIL 400 error : "+CODE_400_DEVICE_ERROR);
				return responseEntity.body(hrv);
			}
			//NO DEVICE TOKEN
			else if(pushRequestVo.getDevice_token()==null || pushRequestVo.getDevice_token().equals("")) {
				logger.info("PUSH FAIL 400 error : "+CODE_400_TOKEN_ERROR);
				return responseEntity.body(hrv);
			}
			
			/* Push Start */
			switch(pushRequestVo.getDevice_type()) {
				case "ios" : 
					//JWT Setting
					pushRequestVo.setKey_id(env.getProperty("apns.keyId"));
					pushRequestVo.setTeam_id(env.getProperty("apns.teamId"));
					pushRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
					pushRequestVo.setApns_topic(env.getProperty("apns.topic"));
					tokenHandler.apnsTokenSet(pushRequestVo);
					
					//POST Message Setting
					jsonPostMessageHandler.waivPostMessageSet(pushRequestVo);
					
					//Request Url Setting
					pushRequestVo.setRequest_url(env.getProperty("apns.url"));
					
					//HttpClient Start
					hrv = httpclient.singlePushStart(pushRequestVo);
					break;
				case "android" : 
					//JWT Setting
					pushRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
					pushRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
					tokenHandler.fcmTokenSet(pushRequestVo);
					
					//Post Message Setting
					jsonPostMessageHandler.waivPostMessageSet(pushRequestVo);
					
					//Request Url Setting
					pushRequestVo.setRequest_url(env.getProperty("fcm.url"));
					
					//HttpClient Start
					hrv = httpclient.singlePushStart(pushRequestVo);
					break;
			}
			int result_code = hrv.getCode();
			String error_message = hrv.getErrorMessage();
			
			//status code가 200으로 return될 경우 PUSH SUCCESS
			if(result_code==200) {
				logger.info("PUSH SUCCESS");
				return ResponseEntity.ok(hrv);
			//잘못된 디바이스 토큰이라는 메세지가 리턴된 경우 400 request error
			}else if(error_message.equals("BadDeviceToken")){
				logger.info("PUSH FAIL 400 error : "+hrv.getErrorMessage());
				return ResponseEntity.status(STATUS_400_CODE).body(hrv);
			}else if(error_message.equals("The registration token is not a valid FCM registration token")){
				logger.info("PUSH FAIL 400 error : "+hrv.getErrorMessage());
				return ResponseEntity.status(STATUS_400_CODE).body(hrv);
			//그외의 케이스인 경우 500 interval server error
			}else{
				logger.info("PUSH FAIL 500 error : "+hrv.getErrorMessage());
				return ResponseEntity.status(STATUS_500_CODE).body(hrv);
			}
		}catch(Exception e) {
			
			//예외 발생 시 500 interval server error
			logger.info("PUSH FAIL 500 error : "+e.toString());
			return ResponseEntity.status(STATUS_500_CODE).body(new PushResponseVo(PUSH_FAIL, e.toString()));
		}
	}

	@Override
	public ResponseEntity<PushResponseVo> visitKoreaPush(PushRequestVo pushRequestVo) {
		// TODO Auto-generated method stub
		
		//HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
		PushResponseVo hrv = new PushResponseVo();
		try {
			switch(pushRequestVo.getDevice_type()) {
			case "ios" : 
				//JWT Setting
				pushRequestVo.setKey_id(env.getProperty("apns.keyId"));
				pushRequestVo.setTeam_id(env.getProperty("apns.teamId"));
				pushRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
				pushRequestVo.setApns_topic(env.getProperty("apns.topic"));
				tokenHandler.apnsTokenSet(pushRequestVo);
				
				//POST Message Setting
				jsonPostMessageHandler.waivPostMessageSet(pushRequestVo);
				
				//Request Url Setting
				pushRequestVo.setRequest_url(env.getProperty("apns.url"));
				
				//HttpClient Start
				hrv = httpclient.multiPushStart(pushRequestVo);
				break;
			case "android" : 
				//JWT Setting
				pushRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
				pushRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
				tokenHandler.fcmTokenSet(pushRequestVo);
				
				//Post Message Setting
				jsonPostMessageHandler.waivPostMessageSet(pushRequestVo);
				
				//Request Url Setting
				pushRequestVo.setRequest_url(env.getProperty("fcm.url"));
				
				//HttpClient Start
				hrv = httpclient.multiPushStart(pushRequestVo);
				break;
		}
			if(hrv.getCode()!=200) {
				return ResponseEntity.status(hrv.getCode()).body(hrv);
			}
		}catch(Exception e) {
			return ResponseEntity.status(STATUS_500_CODE).body(hrv);
		}
		return ResponseEntity.ok(hrv);
	}

	@Override
	public ResponseEntity<PushResponseVo> vkPush(long con_id, long loc_id){
		// TODO Auto-generated method stub
//		PushRequestVo fcmRequestVo = PushRequestVo.builder().key_id(env.getProperty("apns.keyId"))
//															 .team_id(env.getProperty("apns.teamId"))
//															 .keyFile_name(env.getProperty("apns.p8.fileName"))
//															 .apns_topic(env.getProperty("apns.topic"))
//															 .request_url(env.getProperty("apns.url"))
//															 .build();
		PushRequestVo apnsRequestVo = PushRequestVo.builder().request_url(env.getProperty("apns.url"))
															.apns_topic(env.getProperty("apns_topic"))
															.server_token(tokenHandler.getApnsToken(env.getProperty("apns.keyId"),
																									env.getProperty("apns.teamId"),
																									env.getProperty("apns.p8.fileName")))
															.build();
		PushRequestVo fcmRequestVo = PushRequestVo.builder().request_url(env.getProperty("fcm.url"))
															.server_token(tokenHandler.getFcmToken(env.getProperty("fcm.key.fileName")))
															.build();
				
		List<PushMember> iosList = pushMemberDao.pushSendList(loc_id, "ios");
		List<PushMember> androidList = pushMemberDao.pushSendList(loc_id, "android");
		
		return null;
	}
	
	
	
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
//	public HttpResponseVo fcmPush(HttpRequestVo pushRequestVo) throws Exception{
//		logger.info("FCM PUSH START !!!!!");
//		//fcm token setting
//		pushRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
//		pushRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
//		tokenHandler.fcmTokenSet(pushRequestVo);
//		
//		//fcm POST data setting
//		logger.info("FCM PUSH DATA MAKE START !!!!!");
//        JSONObject messageObject = new JSONObject();
//        JSONObject dataObject = new JSONObject();
//        JSONObject androidObject = new JSONObject();
//        JSONObject parentObject = new JSONObject();
//        androidObject.put("priority", "high");
//        dataObject.put("menu", pushRequestVo.getMenu());
//        dataObject.put("id", pushRequestVo.getId());
//        dataObject.put("group_id", pushRequestVo.getGroup_id());
//        dataObject.put("mode", pushRequestVo.getMode());
//        dataObject.put("title", pushRequestVo.getTitle());
//        dataObject.put("from_id", pushRequestVo.getFrom());
//        dataObject.put("message", pushRequestVo.getMessage());
//        messageObject.put("token", pushRequestVo.getDevice_token()); 
//        //messageObject.put("notification", dataObject);
//        messageObject.put("android", androidObject);
//        messageObject.put("data", dataObject);
//        parentObject.put("message", messageObject); // deviceID
//        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
//        pushRequestVo.setPost_data(parentObject.toString());
//        logger.info("FCM PUSH DATA MAKE END !!!!!");
//        
//        //fcm url setting
//        pushRequestVo.setFcm_start_url(env.getProperty("fcm.start.url"));
//        pushRequestVo.setFcm_end_url(env.getProperty("fcm.end.url"));
//        pushRequestVo.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
//        pushRequestVo.setRequest_type(env.getProperty("fcm.req.type"));
//        pushRequestVo.setFcm_full_url(
//        						pushRequestVo.getRequest_type()+
//        						pushRequestVo.getFcm_start_url()+
//        						pushRequestVo.getFcm_end_url()
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
////		return httpclient.httpStart(pushRequestVo);
//        httpclient.httpMultiStart(pushRequestVo);
//        return null;
//	}
//	
//	@Override
//	public HttpResponseVo apnsPush(HttpRequestVo pushRequestVo) throws Exception{
//		logger.info("APNS PUSH START !!!!!");
//		//apns token setting
//		pushRequestVo.setKey_id(env.getProperty("apns.keyId"));
//		pushRequestVo.setTeam_id(env.getProperty("apns.teamId"));
//		pushRequestVo.setKey_path(env.getProperty("apns.keyPath"));
//		pushRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
//	//	pushRequestVo.setApns_keyFile(apnsKeyFile);
//		tokenHandler.apnsTokenSet(pushRequestVo);
//		
//		//apns POST data setting
//		logger.info("APNS PUSH DATA MAKE START !!!!!");
//		JSONObject apsObject = new JSONObject();
//		//JSONObject alertObject = new JSONObject();
//		JSONObject dataObject = new JSONObject();
//		dataObject.put("menu", pushRequestVo.getMenu());
//		dataObject.put("id", pushRequestVo.getId());
//		dataObject.put("group_id", pushRequestVo.getGroup_id());
//		dataObject.put("mode", pushRequestVo.getMode());
//		dataObject.put("title", pushRequestVo.getTitle());
//		dataObject.put("from_id", pushRequestVo.getFrom());
//		dataObject.put("message", pushRequestVo.getMessage());
//		//alertObject.put("alert", dataObject);
//		apsObject.put("aps", dataObject);
//		logger.info("APNS PUSH DATA : {}",apsObject.toString());
//        pushRequestVo.setPost_data(apsObject.toString());
//        logger.info("APNS PUSH DATA MAKE END !!!!!");
//        
//        //apns topic setting
//        pushRequestVo.setApns_topic(env.getProperty("apns.topic"));
//        
//        //apns url setting
//        pushRequestVo.setApns_start_url(env.getProperty("apns.start.url"));
//        pushRequestVo.setApns_end_url(env.getProperty("apns.end.url"));
//        pushRequestVo.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
//        pushRequestVo.setRequest_type(env.getProperty("apns.req.type"));
//        
//        return null;
//	//	return httpclient.http2Start(pushRequestVo);
//	}
//	
//	@Override
//	public void apnsMultiPushTest(HttpRequestVo pushRequestVo) throws Exception{
//		logger.info("APNS PUSH START !!!!!");
//		//apns token setting
//		pushRequestVo.setKey_id(env.getProperty("apns.keyId"));
//		pushRequestVo.setTeam_id(env.getProperty("apns.teamId"));
//		pushRequestVo.setKey_path(env.getProperty("apns.keyPath"));
//		pushRequestVo.setKeyFile_name(env.getProperty("apns.p8.fileName"));
//	//	pushRequestVo.setApns_keyFile(apnsKeyFile);
//		tokenHandler.apnsTokenSet(pushRequestVo);
//		
//		//apns POST data setting
//		logger.info("APNS PUSH DATA MAKE START !!!!!");
//		JSONObject apsObject = new JSONObject();
//		JSONObject dataObject = new JSONObject();
//		dataObject.put("menu", pushRequestVo.getMenu());
//		dataObject.put("id", pushRequestVo.getId());
//		dataObject.put("group_id", pushRequestVo.getGroup_id());
//		dataObject.put("mode", pushRequestVo.getMode());
//		dataObject.put("title", pushRequestVo.getTitle());
//		dataObject.put("from_id", pushRequestVo.getFrom());
//		dataObject.put("message", pushRequestVo.getMessage());
//		apsObject.put("aps", dataObject);
//		logger.info("APNS PUSH DATA : {}",apsObject.toString());
//        pushRequestVo.setPost_data(apsObject.toString());
//        logger.info("APNS PUSH DATA MAKE END !!!!!");
//        
//        //apns topic setting
//        pushRequestVo.setApns_topic(env.getProperty("apns.topic"));
//        
//        //apns url setting
//        pushRequestVo.setApns_start_url(env.getProperty("apns.start.url"));
//        pushRequestVo.setApns_end_url(env.getProperty("apns.end.url"));
//        pushRequestVo.setApns_port(Integer.parseInt(env.getProperty("apns.port")));
//        pushRequestVo.setRequest_type(env.getProperty("apns.req.type"));
//        
//		httpclient.http2MultiStart(pushRequestVo);
//	}
//
//	@Override
//	public void fcmMultiPushTest(HttpRequestVo pushRequestVo) throws Exception {
//		// TODO Auto-generated method stub
//		logger.info("FCM PUSH START !!!!!");
//		//fcm token setting
//		pushRequestVo.setKey_path(env.getProperty("fcm.keyPath"));
//		pushRequestVo.setKeyFile_name(env.getProperty("fcm.key.fileName"));
//		tokenHandler.fcmTokenSet(pushRequestVo);
//		
//		//fcm POST data setting
//		logger.info("FCM PUSH DATA MAKE START !!!!!");
//        JSONObject messageObject = new JSONObject();
//        JSONObject dataObject = new JSONObject();
//        JSONObject androidObject = new JSONObject();
//        JSONObject parentObject = new JSONObject();
//        androidObject.put("priority", "high");
//        dataObject.put("menu", pushRequestVo.getMenu());
//        dataObject.put("id", pushRequestVo.getId());
//        dataObject.put("group_id", pushRequestVo.getGroup_id());
//        dataObject.put("mode", pushRequestVo.getMode());
//        dataObject.put("title", pushRequestVo.getTitle());
//        dataObject.put("from_id", pushRequestVo.getFrom());
//        dataObject.put("message", pushRequestVo.getMessage());
//        messageObject.put("token", pushRequestVo.getDevice_token()); 
//        //messageObject.put("notification", dataObject);
//        messageObject.put("android", androidObject);
//        messageObject.put("data", dataObject);
//        parentObject.put("message", messageObject); // deviceID
//        logger.info("FCM PUSH DATA : {}",parentObject.toString());        
//        pushRequestVo.setPost_data(parentObject.toString());
//        logger.info("FCM PUSH DATA MAKE END !!!!!");
//        
//        //fcm url setting
//        pushRequestVo.setFcm_start_url(env.getProperty("fcm.start.url"));
//        pushRequestVo.setFcm_end_url(env.getProperty("fcm.end.url"));
//        pushRequestVo.setFcm_port(Integer.parseInt(env.getProperty("fcm.port")));
//        pushRequestVo.setRequest_type(env.getProperty("fcm.req.type"));
//        pushRequestVo.setFcm_full_url(
//        						pushRequestVo.getRequest_type()+
//        						pushRequestVo.getFcm_start_url()+
//        						pushRequestVo.getFcm_end_url()
//        					  );
//		FirebaseOptions options = new FirebaseOptions.Builder()
//		  .setCredentials(GoogleCredentials.fromStream(new FileInputStream(new ClassPathResource(pushRequestVo.getKeyFile_name()).getFile())))
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

}
