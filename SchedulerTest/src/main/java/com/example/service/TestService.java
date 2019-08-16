package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.TestDao;
import com.example.vo.TestVo;

@Service
public class TestService {

	@Autowired
	private TestDao testDao;
	
	private static List<TestVo> testList;
	
	public static void testService() {
		//testList = testDa
	}
}
