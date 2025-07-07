package com.kh.spring.security.model.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/*
 * UserDetailsService
 *  - 스프링 시큐리티에서 인증처리시 사용하는 핵심 인터페이스
 *  - 사용자 정보를 조회하는 메서드인 loadUserByUsername()를 정의하고 있다
 *  
 * loadUserByUsername()
 *  - 사용자의 id를 기반으로 UserDetails객체를 반환하는 메서드
 *  - 반환된 USerDetails는 pw및 권한 검증시 사용된다.
 *  - 일반적으로 DB를 이용하여 사용자 정보를 조회하는 비즈니스로직을 작성한다.
 */
public interface SecurityService extends UserDetailsService{

}