package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.SchedulerTestApplication;
import com.example.dao.TestDao;
import com.example.vo.TestVo;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TestController {

	
	
	@Autowired
	private TestDao dao;
	@RequestMapping("/")
	public String test() {
		List<TestVo> list = SchedulerTestApplication.staticList;
		for(int i=0;i<list.size();i++) {
			log.info("id : "+list.get(i).getId()+" , value : "+list.get(i).getValue());
		}
		
		return "";
	}
}
