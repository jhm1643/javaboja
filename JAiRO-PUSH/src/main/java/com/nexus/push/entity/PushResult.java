package com.nexus.push.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushResult {

	private long result_id;
	private long con_id;
	private long lang_id;
	private long total_count;
	private long fail_count;
	private String create_date;
}
