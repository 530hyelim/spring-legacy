package com.kh.spring.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.BoardExt;
import com.kh.spring.member.model.vo.Member;

public class BoardOwnerCheckInterceptor implements HandlerInterceptor{
	// 스프링 시큐리티로 꾸미는게 불가능 => 인터셉터 or AOP
	@Autowired
	private BoardService boardService; // 게시글 작성자정보 조회용
	
	// 전처리(preHandle)함수 : 컨트롤러가 서블릿의 요청을 처리하기 "전"에 먼저 실행되는 함수
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 1. 현재 요청을 보낸 사용자의 정보 추출. session에 있는 데이터로도 뽑아올수 있지만 이 방법이 성능 더 좋음
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Member loginUser = (Member) auth.getPrincipal();
		
		// 3. ADMIN 권한을 가진 경우 항상 통과
		if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return true; // 관리자는 항상 통과
		}
		
		// 2. 요청 uri에서 게시글 번호 추출
		//  /board/update/1001
		String [] uri = request.getRequestURI().split("/");
		int boardNo = Integer.parseInt(uri[uri.length-1]);
		
		BoardExt board = boardService.selectBoard(boardNo);
		
		int boardWriter = Integer.parseInt(board.getBoardWriter());
		if (!(board != null && boardWriter == loginUser.getUserNo())) {
			// 권한없음 페이지로 이동
			response.sendRedirect(request.getContextPath() + "/security/accessDenied");
			return false; // 컨트롤러로 전달 안 함
		}
		return true;
	}
}
