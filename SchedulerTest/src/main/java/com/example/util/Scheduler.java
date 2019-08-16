package com.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.SchedulerTestApplication;
import com.example.dao.TestDao;
import com.example.vo.TestVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Scheduler {

	@Autowired
	private TestDao dao;
	
	@Scheduled(fixedDelay=1000)
	public void scheldulerlog() {
		
		TestVo vo = new TestVo();
		vo.setValue(""+System.currentTimeMillis());
		dao.save(vo);
		SchedulerTestApplication.staticList=dao.findAll();
	}
}
