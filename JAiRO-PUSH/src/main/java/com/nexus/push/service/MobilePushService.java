package com.nexus.push.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.domain.HttpRequestVo;

@Service
public interface MobilePushService {
	public ResponseEntity<HttpResponseVo> waivPush(HttpRequestVo pushDomain);
	public ResponseEntity<HttpResponseVo> visitKoreaPush(HttpRequestVo pushDomain);
	
	public ResponseEntity<HttpResponseVo> vkPush(long loc_id, long lang_id);
//	public HttpResponseVo fcmPush(HttpRequestVo pushDomain) throws Exception;
//	public HttpResponseVo apnsPush(HttpRequestVo pushDomain) throws Exception;
//	public void fcmMultiPushTest(HttpRequestVo pushDomain) throws Exception;
//	public void apnsMultiPushTest(HttpRequestVo pushDomain) throws Exception;
}
