package com.nexus.push.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushResult {
	
	private int code;
	private String message;
	private String errorMessage;
	

	public PushResult() {};
	public PushResult(int code, String errorMessage) {
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
	public PushResult(String message, int code, String errorMessage) {
		this.setMessage(message);
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
}

