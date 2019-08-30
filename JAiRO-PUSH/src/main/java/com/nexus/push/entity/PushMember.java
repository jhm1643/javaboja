package com.nexus.push.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class PushMember {

	private String token_id;
	private long loc_id;
	private long lang_id;
	private String device_type;
	private String phone_num;
	private String location;
	private String language;
	private String longtitude;
	private String latitude;
	private String create_date;
	private String mod_date;
	
}
