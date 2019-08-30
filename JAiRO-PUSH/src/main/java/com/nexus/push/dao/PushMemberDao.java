package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushMember;
import com.nexus.push.entity.PushResult;

@Mapper
public interface PushMemberDao {

	public List<PushMember> selectByLocIdAndLangId(long loc_id, long lang_id);
	public int merge(PushMember pushMember);
	public int delete(PushMember pushMember);
	
}
