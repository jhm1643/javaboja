package api.push;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.simple.JSONObject;

import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;
import com.baidu.yun.push.auth.PushKeyPair;
import com.baidu.yun.push.client.BaiduPushClient;
import com.baidu.yun.push.constants.BaiduPushConstants;
import com.baidu.yun.push.exception.PushClientException;
import com.baidu.yun.push.exception.PushServerException;
import com.baidu.yun.push.model.PushMsgToSingleDeviceRequest;
import com.baidu.yun.push.model.PushMsgToSingleDeviceResponse;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;


public class BAIDUSender {


	protected final Logger logger = Logger.getLogger(getClass().getName());
		
	private static PushKeyPair pair=null;
	
	
	public BAIDUSender(String apikey,String secretkey) {
		if(pair==null){pair = new PushKeyPair(apikey,  secretkey);}
	}
	
	public void send(PushObject po) throws PushClientException,PushServerException{	
		JSONObject json = new JSONObject();
		json.put("application", po.getApplication());
		json.put("uce_menu", po.getMenu());
		json.put("uce_title", po.getTitle());
		json.put("uce_from", po.getFrom());
		json.put("uce_mode", po.getMode());
		json.put("uce_message", po.getMessage());
		
		
		logger.info("####Send Message Info : "+json.toJSONString());
		
		BaiduPushClient client = new BaiduPushClient(pair,BaiduPushConstants.CHANNEL_REST_URL);
		client.setChannelLogHandler(new YunLogHandler() {
			@Override
			public void onHandle(YunLogEvent event) {
				logger.info("####CLIENT HANDLE MESSAGE : "+event.getMessage());
			}
		});
		
		try {
			PushMsgToSingleDeviceRequest request = new PushMsgToSingleDeviceRequest();
			request.addChannelId(po.getToken());//ChannelID 받아야함//사용자 정보
			request.addMessageType(0);//0은 알림창 X 1은 알림
			request.addMessage(json.toJSONString());
			request.addDeviceType(3);//1 WEB, 2 PC, 3 Android, 4 IOS
			PushMsgToSingleDeviceResponse response = client.pushMsgToSingleDevice(request);
		} catch (PushClientException e) {
			if (BaiduPushConstants.ERROROPTTYPE) {
                throw e;
            } else {
                e.printStackTrace();
            }
		} catch (PushServerException e) {
			 if (BaiduPushConstants.ERROROPTTYPE) {
	                throw e;
	            } else {
	                logger.info(String.format(
	                        "requestId: %d, errorCode: %d, errorMsg: %s",
	                        e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
	            }
		}
		
	}

}
