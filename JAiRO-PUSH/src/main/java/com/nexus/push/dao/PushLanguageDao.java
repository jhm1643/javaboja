package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushLanguage;

@Mapper
public interface PushLanguageDao {

	public List<PushLanguage> selectAll();
	public List<PushLanguage> selectById(PushLanguage pushLanguage);
	
}
