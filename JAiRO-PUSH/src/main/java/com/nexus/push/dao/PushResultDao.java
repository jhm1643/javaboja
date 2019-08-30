package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushResult;

@Mapper
public interface PushResultDao {

	public List<PushResult> selectAll();
	public List<PushResult> selectById(PushResult pushResult);
	public int insert(PushResult pushResult);
	public int delete(PushResult pushResult);
}
