package com.nexus.push.httpClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.push.domain.HttpStatusDomain;

public class HttpResponseEntity {
	
	public ResponseEntity<HttpStatusDomain> httpResponse(String result, int code, String errorMessage){
		HttpStatusDomain hsd = new HttpStatusDomain(result, code, errorMessage);
		HttpStatus hs=HttpStatus.valueOf(code);
		ResponseEntity<HttpStatusDomain> rese=new ResponseEntity<HttpStatusDomain>(hsd, hs);
		return rese;
	}
	
	public ResponseEntity<HttpStatusDomain> httpResponse(String result, int code){
		HttpStatusDomain hsd = new HttpStatusDomain(code, result);
		HttpStatus hs=HttpStatus.valueOf(code);
		ResponseEntity<HttpStatusDomain> rese=new ResponseEntity<HttpStatusDomain>(hsd, hs);
		return rese;
	}
	
	
}
