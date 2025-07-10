package com.kh.spring.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class AfterThrowingTest {
	// 반환되는 값이 에러일 때
	@AfterThrowing(pointcut = "CommonPointcut.commonPoint()", throwing = "exceptionObj")
	public void returnException(JoinPoint jp, Exception exceptionObj) {
		StringBuilder sb = new StringBuilder("Exception : " + exceptionObj.getStackTrace()[0]);
		// .getStackTrace()[0] : 최상위 에러 메시지. 근본 원인
		sb.append("에러 메세지 : " + exceptionObj.getMessage() + "\n");
		log.error(sb.toString());
	}
}
