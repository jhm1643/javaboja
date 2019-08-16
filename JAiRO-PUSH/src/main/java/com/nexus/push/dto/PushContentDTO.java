package com.nexus.push.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushContentDTO {

	private long idx;
	private String type;
	private String title;
	private String content;
	private String create_date;
	private String mod_date;
}
