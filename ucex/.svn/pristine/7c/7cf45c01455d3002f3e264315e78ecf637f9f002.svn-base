package api.push;
 
import java.io.IOException;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.yun.push.exception.PushClientException;
import com.baidu.yun.push.exception.PushServerException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;


import api.config.PushProperties;
import api.dao.PushDao;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.notification.PushNotificationBigPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import javapns.notification.ResponsePacket;

@Component
@Configuration
@EnableConfigurationProperties(PushProperties.class)
@Scope("prototype")
public class PushService implements Runnable {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	private PushProperties pushProperties;
	
	private static Logger logger = LoggerFactory.getLogger(PushService.class);
	
	private PushObject po;

	public void setPo(PushObject po) {
		this.po = po;
	}

	private void pushBAIDU(PushObject po) {
		logger.info("android BAIDO push... [{}]", po.getToken());
		BAIDUSender sender = new BAIDUSender(pushProperties.getBaiduapikey(),pushProperties.getBaidusecretkey());
		try {
			sender.send(po);
		} catch (Exception e){
			logger.error(e.toString(), e);
		}
	}
	private boolean pushGCM(PushObject po) {
		logger.info("android GCM push... [{}]", po.getInvokeid());
		
		GCMSender sender = new GCMSender(pushProperties.getGcmkey()); // 서버 API Key 입력

		Builder mb = new Message.Builder();
		mb.timeToLive(pushProperties.getTimetolive());

		mb.addData("uce_menu", po.getMenu());
		mb.addData("uce_title", po.getTitle());
		mb.addData("uce_from", po.getFrom());
		mb.addData("uce_mode", po.getMode());
		mb.addData("uce_message", po.getMessage());
		
		Message message = mb.build();
		GCMResult result = null;
		
		try {
			///////////////////////////////////////////////////////////
			//2018.11.01 martino Push Service pushProperties.getRetry()=push.retry=3
			result = sender.send(message, po.getToken(), pushProperties.getRetry(), "high");
			//logger.info("gcm getErrorCodeName:{}", result.getErrorCodeName());
		
			po.setResultCode(result.getMessageId());
			po.setResultMessage(result.getErrorCodeName());
			
		} catch (IOException e) {
			logger.error(e.toString(), e);
		}

		///////////////////////////////////////////////////////////
		//2018.11.01 martino Push Service log 보강
		logger.info("gcm push [{}] MessageId:{} ErrorCodeName:{} result:{}", po.getInvokeid(), result.getMessageId(), result.getErrorCodeName(), result.toString());
		//logger.info("gcm push getResultMessage:{}", po.getResultMessage());

		///////////////////////////////////////////////////////////
		//2018.11.01 martino Push Service DB insert
		//<mapper namespace="api.dao.PushDao">
		//	<insert id="insertPushLog" parameterType="api.push.PushObject"> 이 호출
		PushDao dao =  sqlSessionTemplate.getMapper(PushDao.class);
		dao.insertPushLog(po);
		
		///////////////////////////////////////////////////////////
		//2018.11.01 martino Push Service 가 실패하면 ExtensionToken delete
		//   - Call getMessageId(): null means error, call getErrorCodeName()
		//							non-null means the message was created
		if ( po.getResultCode() == null ) {
			if ( po.getResultMessage().equalsIgnoreCase("NotRegistered") ) {
				//단말 삭제 처리
				doFeedBack(po.getToken());
			}
		}			
		return false;
	}

	private boolean pushAPNS(PushObject po) {
		logger.info("ios APNS push... [{}]", po.getInvokeid());
		
		int failed = 0;
		int successful = 0;
		PushedNotifications notifications = null;
		
		PushNotificationBigPayload payload = new PushNotificationBigPayload();
		
        try {
			payload.addAlert(po.getTitle());
//			payload.addBadge(1);
			payload.addSound("default");
			
			payload.addCustomDictionary("menu", po.getMenu());
			payload.addCustomDictionary("title", po.getTitle());
			payload.addCustomDictionary("from", po.getFrom());
			payload.addCustomDictionary("mode", po.getMode());
			payload.addCustomDictionary("message", po.getMessage());
			payload.setExpiry(pushProperties.getTimetolive());

			///////////////////////////////////////////////////////////
			//2018.11.01 martino Push Service pushProperties.getRetry()=push.retry=3
			for ( int i=0; i < pushProperties.getRetry(); ++i) {
				
				if (i > 0) {
					logger.info("apns push [{}] retry {}", po.getInvokeid(), i);
				}
				
				notifications = Push.payload(payload, pushProperties.getApnscert(), pushProperties.getApnspassword(), pushProperties.isApnsdev(), po.getToken());
				
				successful = notifications.getSuccessfulNotifications().size();
				failed = notifications.getFailedNotifications().size();					
						
				///////////////////////////////////////////////////////////
				//2018.11.01 martino Push Service log 보강
				logger.info("apns push [{}] result success/fail : {}", po.getInvokeid(), successful + "/" + failed);
				logger.info("apns push message [{}]", notifications.toString());
				
				if (successful > 0) {
					failed = 0;
					po.setResultCode( "0" );
					po.setResultMessage("SUCCESS");
					break;
				}
				
				Thread.sleep(100);
			}
			
			if (failed > 0) {
				PushedNotification f = notifications.getFailedNotifications().get(0);
				ResponsePacket response = f.getResponse();
				
				// response 는 NULL 일때도 있고 아닐때도 있다.
				if ( response == null ) {
					po.setResultCode( "99" );
					po.setResultMessage(f.getException().getMessage());
				} else {
					po.setResultCode( String.valueOf(response.getStatus()) );
					po.setResultMessage( response.getMessage() );
				}
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}

		PushDao dao =  sqlSessionTemplate.getMapper(PushDao.class);
		dao.insertPushLog(po);
		
		return false;
	}
	
	public void feedbackAPNS() throws CommunicationException, KeystoreException {
		List<Device> deviceList = Push.feedback(pushProperties.getApnscert(), pushProperties.getApnspassword(), pushProperties.isApnsdev());
		
		logger.info("FEEDBACKED DEVICE LIST : {}", deviceList);
		for (Device d : deviceList) {
			logger.info("FEED BACKED DEVICE : id[{}], token[{}]", d.getDeviceId(), d.getToken());
			doFeedBack(d.getToken());
		}
	}
	
	@Override
	public void run() {
		if (po == null)
			return;
		
		switch (po.getDevice()) {
				
			case "ios":
			pushAPNS(po);
				break;
				
			case "android":
				if(pushProperties.getAndroidpushserver().equals("0")){
					pushGCM(po);				
				}else{
					pushBAIDU(po);
				}
				
				break;
				
			default:
				logger.error("unkown device type ! ... {}", po.getDevice());
				break;
		}
	}

	public void deleteOldPushLogs() {
		logger.info("deleteOldPushLogs SCHEDULER...");
		PushDao dao =  sqlSessionTemplate.getMapper(PushDao.class);
		dao.deleteOldPushLogs(pushProperties.getPush_log_period());
	}

//	@Autowired
//	private Rabbit r;
	
	private void doFeedBack(String token) {
		logger.info("deleteExtensionToken token ... {}", token);

		///////////////////////////////////////////////////////////
		//2018.11.01 martino Push Service ExtensionToken delete
		//<update id="deleteExtensionToken" parameterType="string">
		//update uc_extension set uce_os_type=null, uce_push_key=null where uce_push_key = #{token}
		PushDao dao =  sqlSessionTemplate.getMapper(PushDao.class);
		dao.deleteExtensionToken(token);
		
		/*
		StringBuffer sb = new StringBuffer();
		
		sb.append("PUSHFEEDBACK").append("|").append(token).append("\0");
		
		try {
			r.sendCubeMessage(sb.toString());
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		*/
	}
}
