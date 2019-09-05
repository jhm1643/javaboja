package com.nexus.push.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nexus.push.domain.PushResponseVo;
import com.nexus.push.domain.PushRequestVo;

@Service
public interface MobilePushService {
	public ResponseEntity<PushResponseVo> waivPush(PushRequestVo pushDomain);
	public ResponseEntity<PushResponseVo> visitKoreaPush(PushRequestVo pushDomain);
	
	public ResponseEntity<PushResponseVo> vkPush(long loc_id, long lang_id);
//	public HttpResponseVo fcmPush(HttpRequestVo pushDomain) throws Exception;
//	public HttpResponseVo apnsPush(HttpRequestVo pushDomain) throws Exception;
//	public void fcmMultiPushTest(HttpRequestVo pushDomain) throws Exception;
//	public void apnsMultiPushTest(HttpRequestVo pushDomain) throws Exception;
}
