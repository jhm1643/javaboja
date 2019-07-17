package com.nexus.push.util;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ThreadTest {

	@Async
	public void threadStart(int i) {
		logger.info("carrey : "+i);
	}
}
