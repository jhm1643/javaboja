package com.nexus.push.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushResponseVo {
	
	private int code;
	private String message="";
	private String errorMessage="";
	

	public PushResponseVo() {};
	public PushResponseVo(int code, String errorMessage) {
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
	public PushResponseVo(int code, String message, String errorMessage) {
		this.setMessage(message);
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
	public PushResponseVo(String message, String errorMessage) {
		this.setMessage(message);
		this.setErrorMessage(errorMessage);
	}

//	public HttpResponseVo message(String message) {
//		HttpResponseVo hrv = new HttpResponseVo();
//		hrv.setCode(getCode());
//		hrv.setErrorMessage(getErrorMessage());
//		hrv.setMessage(message);
//		return hrv;
//	}
}

