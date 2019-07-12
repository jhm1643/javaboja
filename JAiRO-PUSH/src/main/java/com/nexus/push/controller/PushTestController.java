package com.nexus.push.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.messaging.MulticastMessage;
import com.nexus.push.domain.HttpStatusDomain;
import com.nexus.push.domain.PushDomain;
import com.nexus.push.httpClient.HttpResponseEntity;
import com.nexus.push.service.PushServiceImpl;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PushTestController {
	
	@Autowired
	private PushServiceImpl pushService;
	
	static final String CODE_400_DATA_ERROR="Received data is null";
	static final String CODE_400_DEVICE_ERROR="Device name is android or ios";
	static final String CODE_400_TOKEN_ERROR="Token value is empty";

	@RequestMapping(value = "/nexus/push/test" , method = RequestMethod.POST)
	public ResponseEntity<HttpStatusDomain> push(@RequestBody PushDomain pushDomain,HttpServletRequest request, HttpServletResponse res){
		logger.info("sadfsdfsdfasdfdsajfosadjfoasdf");
		logger.info("PUSH EXECUTE!!!");
		HttpResponseEntity resResult = new HttpResponseEntity();
		HttpStatusDomain httpStatusDomain= new HttpStatusDomain();
		try{
			//NO DATA
			if(pushDomain==null) {
				logger.info("PUSH FAIL 400 error : "+CODE_400_DATA_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,CODE_400_DATA_ERROR);
			}
			//NO DEVICE
			else if(pushDomain.getDevice()==null || pushDomain.getDevice().equals("")){
				logger.info("PUSH FAIL 400 error : "+CODE_400_DEVICE_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,CODE_400_DEVICE_ERROR);
			}else if(!(pushDomain.getDevice().equals("ios") || pushDomain.getDevice().equals("android"))){
				logger.info("PUSH FAIL 400 error : "+CODE_400_DEVICE_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,CODE_400_DEVICE_ERROR);
			}
			//NO DEVICE TOKEN
			else if(pushDomain.getDevice_token()==null || pushDomain.getDevice_token().equals("")) {
				logger.info("PUSH FAIL 400 error : "+CODE_400_TOKEN_ERROR);
				return resResult.httpResponse("PUSH FAIL",400,CODE_400_TOKEN_ERROR);
			}
			
			//PUSH START
			switch(pushDomain.getDevice()) {
				case "ios" : 
					httpStatusDomain=pushService.apnsPush(pushDomain);
					break;
				case "android" : 
					logger.info("asdfasfsadfasdfsadfasdf");
					pushService.fcmPushTest(pushDomain);
					break;
			}
			int result_code = httpStatusDomain.getCode();
			String error_message = httpStatusDomain.getErrorMessage();
			
			//status code가 200으로 return될 경우 PUSH SUCCESS
			if(result_code==200) {
				logger.info("PUSH SUCCESS");
				return resResult.httpResponse("PUSH SUCCESS",200,"");
			
			//잘못된 디바이스 토큰이라는 메세지가 리턴된 경우 400 request error
			}else if(error_message.equals("BadDeviceToken")){
				logger.info("FCM PUSH TEST SUCCESS!");
				return resResult.httpResponse("FCM PUSH TEST SUCCESS!",200,httpStatusDomain.getErrorMessage());
			}else if(error_message.equals("The caller does not have permission")){
				logger.info("APNS PUSH TEST SUCCESS!");
				return resResult.httpResponse("APNS PUSH TEST SUCCESS!",200,httpStatusDomain.getErrorMessage());
			}else if(error_message.equals("The registration token is not a valid FCM registration token")){
				logger.info("PUSH FAIL 400 error : "+httpStatusDomain.getErrorMessage());
				return resResult.httpResponse("PUSH FAIL",400,httpStatusDomain.getErrorMessage());
			
			//그외의 케이스인 경우 500 interval server error
			}else{
				logger.info("PUSH FAIL 500 error : "+httpStatusDomain.getErrorMessage());
				return resResult.httpResponse("PUSH FAIL",500,httpStatusDomain.getErrorMessage());
			}
		}catch(Exception e) {
			
			//예외 발생 시 500 interval server error
			logger.info("PUSH FAIL 500 error : "+e.toString());
			return resResult.httpResponse("PUSH FAIL",500,e.toString());
		}
	}
}
