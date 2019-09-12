package com.nexus.push.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PushLanguage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long lang_id;
	private String language;
}
