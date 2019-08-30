package com.nexus.push.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class PushTokenHistory {

	private long history_id;
	private String device_token;
	private String device_type;
	private String create_date;
}
