package com.kh.spring.member.model.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.spring.member.model.dao.MemberDao;
import com.kh.spring.member.model.vo.Member;

@Service // component-scan에 의해 bean 객체로 등록될 클래스를 지정
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;
	

	@Override
	public Member loginMember(Member m) {
		// 세션데이터를 스프링이 알아서 관리해줘서 dao 메서드 호출만 해주면 됨
		return memberDao.loginMember(m);
	}
	
	@Override
	public Member loginMember(String userId) {

		return null;
	}

	@Override
	public int insertMember(Member m) {
		int result = memberDao.insertMember(m);
		// 회원 ID와 기본 USER 권한 추가
		memberDao.insertAuthority(m);
		return result;
	}

	@Override
	public int updateMember(Member m) {
		return memberDao.updateMember(m);
	}

	@Override
	public int idCheck(String userId) {
		return memberDao.idCheck(userId);
	}

	@Override
	public void updateMemberChagePwd() {

	}

	@Override
	public HashMap<String, Object> selectOne(String userId) {
		return memberDao.selectOne(userId);
	}
}
