package com.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.dao.TestDao;
import com.example.vo.TestVo;

@SpringBootApplication
@EnableScheduling
public class SchedulerTestApplication {

	public static List<TestVo> staticList;
	
	@Autowired
	private TestDao dao;
	public static void main(String[] args) {
		SpringApplication.run(SchedulerTestApplication.class, args);
	}

	@Bean
	public void insertData() {
		TestVo vo = new TestVo();
		vo.setValue("a");
		dao.save(vo);
		staticList = dao.findAll();
	}
}
