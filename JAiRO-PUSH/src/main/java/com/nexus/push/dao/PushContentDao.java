package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;

@Mapper
public interface PushContentDao {

	public List<PushContent> selectAll();
	public PushContent selectByConId(long con_id);
	
}
