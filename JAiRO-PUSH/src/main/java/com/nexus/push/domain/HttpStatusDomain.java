package com.nexus.push.domain;

import lombok.Data;

@Data
public class HttpStatusDomain {
	
	private int code;
	private String message;
	private String errorMessage;
	

	public HttpStatusDomain() {};
	public HttpStatusDomain(int code, String errorMessage) {
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
	public HttpStatusDomain(String message, int code, String errorMessage) {
		this.setMessage(message);
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}
}

