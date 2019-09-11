package com.nexus.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.nexus.push.entity.PushContent;
import com.nexus.push.entity.PushMember;
import com.nexus.push.entity.PushResult;

@Mapper
public interface PushMemberDao {

	public List<PushMember> selectByLocId(long loc_id);
	public int merge(PushMember pushMember);
	public int delete(PushMember pushMember);
	public List<PushMember> pushSendList(long loc_id, long con_id);
}
