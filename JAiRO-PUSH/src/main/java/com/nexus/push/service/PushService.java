package com.nexus.push.service;

import org.springframework.stereotype.Service;

import com.nexus.push.domain.HttpStatusDomain;
import com.nexus.push.domain.PushDomain;

@Service
public interface PushService {
	public HttpStatusDomain fcmPush(PushDomain pushDomain) throws Exception;
	public HttpStatusDomain apnsPush(PushDomain pushDomain) throws Exception;
	public void fcmMultiPushTest(PushDomain pushDomain) throws Exception;
	public void apnsMultiPushTest(PushDomain pushDomain) throws Exception;
	public void threadService();
}
