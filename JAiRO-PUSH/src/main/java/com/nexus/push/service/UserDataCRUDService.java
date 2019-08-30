package com.nexus.push.service;

import org.springframework.http.ResponseEntity;

import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.entity.PushMember;

public interface UserDataCRUDService {

	public ResponseEntity<HttpResponseVo> post(PushMember pushMember);
}
