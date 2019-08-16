package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.dto.PushContentDTO;

@Mapper
public interface PushContentDao {

	@Select("select "
				+ "idx,"
				+ "type,"
				+ "title,"
				+ "content,"
				+ "DATE_FORMAT(create_date, '%Y-%m-%d') create_date,"
				+ "DATE_FORMAT(mod_date, '%Y-%m-%d') mod_date"
			+ " from t_push_content")
	public List<PushContentDTO> pushContentList();
}
