package com.nexus.push.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PushTokenHistory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long history_id;
	private String device_token;
	private String device_type;
	private String create_date;
	public PushTokenHistory() {}
	@Builder
	public PushTokenHistory(long history_id, String device_token, String device_type) {
		this.history_id=history_id;
		this.device_token=device_token;
		this.device_type=device_type;
	}
}
