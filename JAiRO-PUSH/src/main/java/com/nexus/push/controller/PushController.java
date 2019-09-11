package com.nexus.push.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.push.domain.PushResponseVo;
import com.nexus.push.entity.PushMember;
import com.nexus.push.domain.PushRequestVo;
import com.nexus.push.service.MobilePushService;
import com.nexus.push.service.UserDataCRUDService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PushController {
	
	@Autowired
	private MobilePushService mobilePushService;
	@Autowired
	private UserDataCRUDService userDataCRUDService;
	
	@PostMapping(value = "/nexus/waiv/push")
	public ResponseEntity<PushResponseVo> waivPush(@RequestBody PushRequestVo pushRequestVo,HttpServletRequest request, HttpServletResponse res){
		return mobilePushService.waivPush(pushRequestVo);
	}
	
//	@PostMapping(value = "/nexus/visitkorea/push")
//	public ResponseEntity<HttpResponseVo> visitPush(@RequestBody HttpRequestVo pushRequestVo,HttpServletRequest request, HttpServletResponse res){
////		List<String> device_token_list = new ArrayList<>();
////		device_token_list.add(pushRequestVo.getDevice_token());
////		pushRequestVo.setDevice_token_list(device_token_list);
//		return pushService.visitKoreaPush(pushRequestVo);
//	}
	
//	@PostMapping(value = "/nexus/vk/push/user/{device_type}")
//	public ResponseEntity<HttpResponseVo> userMerge(@RequestParam(value = "token_id") String token_id,
//													@PathVariable(value = "device_type") String device_type,
//													@RequestParam(value = "phone_num", defaultValue = "") String phone_num,
//													@RequestParam(value = "location", defaultValue = "") String location,
//													@RequestParam(value = "language", defaultValue = "") String language,
//													@RequestParam(value = "longtitude", defaultValue = "") String longtitude,
//													@RequestParam(value = "latitude", defaultValue = "") String latitude
//													){
//		return userDataCRUDService.post(token_id, device_type, phone_num, location, language, longtitude, latitude);
//	}
	
	@PostMapping(value = "/nexus/vk/push/user/{device_type}")
	public ResponseEntity<PushResponseVo> userMerge(@PathVariable(value = "device_type") String device_type,
													@RequestBody PushMember pushMember){
		logger.info("carrey : "+pushMember.toString());
		pushMember.setDevice_type(device_type);
		return userDataCRUDService.post(pushMember);
	}
	
	@GetMapping(value = "/nexus/vk/push/send/{loc_id}/{con_id}")
	public ResponseEntity<PushResponseVo> visitPush(@PathVariable(value = "loc_id") long loc_id,
													@PathVariable(value = "con_id") long con_id){
		mobilePushService.vkPush(loc_id, con_id);
		return null;
	}
	
//	@RequestMapping(value = "/nexus/visitkorea/pushcontent", method = RequestMethod.GET)
//	public void getPushContent(){
//		List<PushContentDTO> list = testService.getPushContentList();
//		for(int i=0;i<list.size();i++) {
//			logger.info("carrey : "+list.get(i).getTitle());
//		}
//	}
	
}
