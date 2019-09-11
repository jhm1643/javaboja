package com.nexus.push.util;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.nexus.push.domain.PushRequestVo;
import com.nexus.push.entity.PushContent;

@Component
public class JsonPostMessageHandler {
	
	public void waivPostMessageSet(PushRequestVo pushRequestVo) {
		if(pushRequestVo.getDevice_type().equals("android")) {
			JSONObject messageObject = new JSONObject();
	        JSONObject dataObject = new JSONObject();
	        JSONObject androidObject = new JSONObject();
	        JSONObject parentObject = new JSONObject();
	        androidObject.put("priority", "high");
	        dataObject.put("menu", pushRequestVo.getMenu());
	        dataObject.put("id", pushRequestVo.getId());
	        dataObject.put("group_id", pushRequestVo.getGroup_id());
	        dataObject.put("mode", pushRequestVo.getMode());
	        dataObject.put("title", pushRequestVo.getTitle());
	        dataObject.put("from_id", pushRequestVo.getFrom());
	        dataObject.put("message", pushRequestVo.getMessage());
	        messageObject.put("token", pushRequestVo.getDevice_token()); 
	        //messageObject.put("notification", dataObject);
	        messageObject.put("android", androidObject);
	        messageObject.put("data", dataObject);
	        parentObject.put("message", messageObject); // deviceID
	        pushRequestVo.setPost_data(parentObject.toString());
	        
		}else if(pushRequestVo.getDevice_type().equals("ios")) {
			JSONObject apsObject = new JSONObject();
			JSONObject dataObject = new JSONObject();
			dataObject.put("menu", pushRequestVo.getMenu());
			dataObject.put("id", pushRequestVo.getId());
			dataObject.put("group_id", pushRequestVo.getGroup_id());
			dataObject.put("mode", pushRequestVo.getMode());
			dataObject.put("title", pushRequestVo.getTitle());
			dataObject.put("from_id", pushRequestVo.getFrom());
			dataObject.put("message", pushRequestVo.getMessage());
			apsObject.put("aps", dataObject);
			pushRequestVo.setPost_data(apsObject.toString());
		}          
	}
}
