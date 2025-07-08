package com.kh.spring.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j // 롬복에서 만든 자동 코드생성 기능
// 원래 필드에 log 객체 생성해야 하는데 자동으로 생성해준거임
public class LogTest {
	/*
	 * Logging Level (레벨별로 메서드가 있음)
	 *  - fatal : 치명적 에러를 의미(현재 버전에서는 존재하지 않는 레벨)
	 *  - error : 요청 처리 중 발생하는 오류에 사용하는 메서드 (e.printStackTrace를 대체)
	 *  - warn : 경고성 메시지 작성 시 사용하는 메서드
	 *  - info : 요청처리 중 발생하는 정보성 메시지 출력 시 사용하는 메서드
	 *  - debug : 개발중에 필요한 정보성 메시지 출력 시 사용
	 *  - trace : 가장 상세한 로깅 레벨로 디버그보다 많은 내부 정보를 출력해준다
	 */
	public static void main(String[] args) {
		log.error("error - {}", "에러메세지");
		log.warn("warn - {}", "경고메세지");
		log.info("info - {}", "인포메세지");
		log.debug("debug - {}", "디버그");
		log.trace("trance - {}", "트레이스");
	}
}
