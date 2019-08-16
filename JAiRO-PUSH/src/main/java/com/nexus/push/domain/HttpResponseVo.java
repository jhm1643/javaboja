package com.nexus.push.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponseVo {
	
	private int code;
	private String message;
	private String errorMessage;
	

	public HttpResponseVo() {};
	public HttpResponseVo(int code, String message) {
		this.setCode(code);
		this.setMessage(message);
	}
	public HttpResponseVo(String message, int code, String errorMessage) {
		this.setMessage(message);
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
}

