package com.nexus.push.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long con_id;
	private String type;
	private String title;
	private String contents_KR;
	private String contents_EN;
	private String contents_JA;
	private String contents_CH;
	private Timestamp create_date;
	private Timestamp mod_date;
}
