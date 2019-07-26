package com.nexus.push.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.dto.PushContentDTO;
import com.nexus.push.domain.HttpRequestVo;
import com.nexus.push.service.PushServiceImpl;
import com.nexus.push.service.TestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PushController {
	
	@Autowired
	private PushServiceImpl pushService;
	@Autowired
	private TestService testService;
	
	
	@RequestMapping(value = "/nexus/waiv/push" , method = RequestMethod.POST)
	public ResponseEntity<HttpResponseVo> waivPush(@RequestBody HttpRequestVo pushRequestVo,HttpServletRequest request, HttpServletResponse res){
		return pushService.waivPush(pushRequestVo);
	}
	
	@RequestMapping(value = "/nexus/visitkorea/push" , method = RequestMethod.POST)
	public ResponseEntity<HttpResponseVo> visitPush(@RequestBody HttpRequestVo pushRequestVo,HttpServletRequest request, HttpServletResponse res){
//		List<String> device_token_list = new ArrayList<>();
//		device_token_list.add(pushRequestVo.getDevice_token());
//		pushRequestVo.setDevice_token_list(device_token_list);
		return pushService.visitKoreaPush(pushRequestVo);
	}
	
	@RequestMapping(value = "/nexus/visitkorea/pushcontent", method = RequestMethod.GET)
	public void getPushContent(){
		List<PushContentDTO> list = testService.getPushContentList();
		for(int i=0;i<list.size();i++) {
			logger.info("carrey : "+list.get(i).getTitle());
		}
	}
}
