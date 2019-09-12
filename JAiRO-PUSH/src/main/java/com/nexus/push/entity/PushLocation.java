package com.nexus.push.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PushLocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long loc_id;
	private String big_city_name;
	private String middle_city_name;
}
