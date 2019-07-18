package com.nexus.push.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nexus.push.domain.PushResult;
import com.nexus.push.domain.PushRequestObject;

@Service
public interface PushService {
	public ResponseEntity<PushResult> push(PushRequestObject pushDomain);
	public PushResult fcmPush(PushRequestObject pushDomain) throws Exception;
	public PushResult apnsPush(PushRequestObject pushDomain) throws Exception;
	public void fcmMultiPushTest(PushRequestObject pushDomain) throws Exception;
	public void apnsMultiPushTest(PushRequestObject pushDomain) throws Exception;
}
