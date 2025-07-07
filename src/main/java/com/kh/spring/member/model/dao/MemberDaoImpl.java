package com.kh.spring.member.model.dao;

import java.util.HashMap;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.spring.member.model.vo.Member;

@Repository
public class MemberDaoImpl implements MemberDao{

	@Autowired
	private SqlSessionTemplate session;
	// DML이 하나의 테이블에만 추가되는게 아니라 복잡하게 여러개 추가되거나 할 때에만 셀프로 트랜잭션 관리하고
	// 간단할 경우에는 스프링이 오토커밋
	
	@Override
	public Member loginMember(Member m) {
		return session.selectOne("member.loginMember", m);
	}
	
	@Override
	public Member loginUser(String userId) {
		
		return null;
	}

	@Override
	public int insertMember(Member m) {
		return session.insert("member.insertMember", m);
	}

	@Override
	public int updateMember(Member m) {
		return session.update("member.updateMember", m);
	}

	@Override
	public int idCheck(String userId) {
		return session.selectOne("member.idCheck", userId);
	}

	@Override
	public void updateMemberChagePwd() {
		
	}

	@Override
	public HashMap<String, Object> selectOne(String userId) {
		return session.selectOne("member.selectOne", userId);
	}

	@Override
	public void insertAuthority(Member m) {
		session.insert("member.insertAuthority", m);
	}
}