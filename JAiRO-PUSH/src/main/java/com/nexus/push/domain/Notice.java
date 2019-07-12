package com.nexus.push.domain;

import lombok.Data;

@Data
public class Notice {
	private String notice_id;
	private String notice_title;
	private String notice_coments;
	private String use_yn;
	private String mod_date;
	private int startIndex;
	private int cntPerPage;
}
