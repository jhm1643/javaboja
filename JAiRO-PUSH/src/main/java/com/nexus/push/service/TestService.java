package com.nexus.push.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexus.push.dao.PushContentDao;
import com.nexus.push.dto.PushContentDTO;

@Service
public class TestService {

	@Autowired
	private PushContentDao pushContentDao;
	
	public List<PushContentDTO> getPushContentList(){
		return pushContentDao.pushContentList();
	}
}
