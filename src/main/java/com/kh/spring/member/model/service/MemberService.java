package com.kh.spring.member.model.service;

import java.util.HashMap;

import com.kh.spring.member.model.vo.Member;

public interface MemberService {

	// 스프링 시큐리티 어쩌구..
	Member loginMember(String userId);

	int insertMember(Member m);

	int updateMember(Member m);

	int idCheck(String userId);

	void updateMemberChagePwd();

	Member loginMember(Member m);

	HashMap<String, Object> selectOne(String userId);

}
