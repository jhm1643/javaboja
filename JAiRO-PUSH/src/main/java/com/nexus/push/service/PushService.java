package com.nexus.push.service;

import com.nexus.push.domain.HttpStatusDomain;
import com.nexus.push.domain.PushDomain;

public interface PushService {
	public HttpStatusDomain fcmPush(PushDomain pushDomain) throws Exception;
	public HttpStatusDomain apnsPush(PushDomain pushDomain) throws Exception;
	public void fcmPushTest(PushDomain pushDomain) throws Exception;
}
