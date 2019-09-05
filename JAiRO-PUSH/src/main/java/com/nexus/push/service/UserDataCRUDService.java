package com.nexus.push.service;

import org.springframework.http.ResponseEntity;

import com.nexus.push.domain.PushResponseVo;
import com.nexus.push.entity.PushMember;

public interface UserDataCRUDService {

	public ResponseEntity<PushResponseVo> post(PushMember pushMember);
}
