package com.nexus.push.domain;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import com.nexus.push.entity.PushMember;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PushFcmVo {

	//fcm domain
		private String fcm_start_url;
		private String fcm_end_url;
		private int fcm_port;
		private String fcm_full_url;
		private JSONObject fcm_data;
		
		//apns domain
		private String apns_topic;
		private String team_id;
		private String key_id;	
		private String apns_start_url;
		//private String apns_start_url2;
		private String apns_end_url;
		private int apns_port;
		private String apns_full_url;
		private File apns_keyFile;
		
		//common domain
		private String title;
		private String server_token;
		private String message;
		private String device_type;  // ios or android
		private String device_token;	// device token
		private List<PushMember> pushSendList;
		private String post_data;
		private String post_data_KR;
		private String post_data_EN;
		private String post_data_JA;
		private String post_data_CH;
		private String request_type;
		private String key_path;
		private String keyFile_name;
		private String menu;
		private String id;
		private String group_id;
		private String mode;
		private String from;
		private String request_url;
}
