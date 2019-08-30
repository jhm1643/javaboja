package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushLocation;

@Mapper
public interface PushLocationDao {

	public List<PushLocation> selectAll();
	public List<PushLocation> selectById(PushLocation pushLocation);
	
}
