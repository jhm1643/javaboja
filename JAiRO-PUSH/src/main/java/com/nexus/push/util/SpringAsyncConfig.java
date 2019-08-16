package com.nexus.push.util;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class SpringAsyncConfig extends AsyncConfigurerSupport{

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1000);
		executor.setMaxPoolSize(1000);
		executor.setKeepAliveSeconds(60);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("heowc-async-");
		executor.initialize();
		return executor;
	}
}
