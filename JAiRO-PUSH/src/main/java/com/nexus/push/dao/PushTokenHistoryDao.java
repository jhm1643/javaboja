package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nexus.push.entity.PushTokenHistory;

@Mapper
public interface PushTokenHistoryDao {

	public List<PushTokenHistory> selectAll();
	public PushTokenHistory selectById(PushTokenHistory pushTokenHistory);
	public int insert(PushTokenHistory pushTokenHistory);
	public int delete(PushTokenHistory pushTokenHistory);
}
