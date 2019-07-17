package com.nexus.push.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.push.service.PushService;
import com.nexus.push.util.ThreadTest;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ThreadController {

	@Autowired
	private PushService ps;
	
	@GetMapping("/async")
	public String goAsync() {
		logger.info("시작!!");
		ps.threadService();
		
		return "";
	}
}
