package com.nexus.push.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponseEntity {
	
	public ResponseEntity<PushResult> httpResponse(String result, int code, String errorMessage){
		PushResult hsd = new PushResult(result, code, errorMessage);
		HttpStatus hs=HttpStatus.valueOf(code);
		ResponseEntity<PushResult> rese=new ResponseEntity<PushResult>(hsd, hs);
		return rese;
	}
	
	public ResponseEntity<PushResult> httpResponse(String result, int code){
		PushResult hsd = new PushResult(code, result);
		HttpStatus hs=HttpStatus.valueOf(code);
		ResponseEntity<PushResult> rese=new ResponseEntity<PushResult>(hsd, hs);
		return rese;
	}
	
	
}
