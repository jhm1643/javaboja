package com.nexus.push.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PushMember implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String token_id;
	private long loc_id;
	private long lang_id;
	private String con_id;
	private String contents;
	private String device_type;
	private String phone_num;
	private String location;
	private String language;
	private String longtitude;
	private String latitude;
	private String create_date;
	private String mod_date;
}
