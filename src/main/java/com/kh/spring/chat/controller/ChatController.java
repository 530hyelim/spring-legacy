package com.kh.spring.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.chat.model.service.ChatService;
import com.kh.spring.chat.model.vo.ChatMessage;
import com.kh.spring.chat.model.vo.ChatRoom;
import com.kh.spring.chat.model.vo.ChatRoomJoin;
import com.kh.spring.member.model.vo.Member;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@Slf4j
@SessionAttributes({"chatRoomNo"}) // model 영역은 리퀘스트 스코프인데 세션어트리뷰트의 키값과 동일하게 담으면
// 리퀘스트가 아니라 세션스코프로 저장됨 => Model 영역에 추가하는 key 값이 chatRoomNo인 데이터를 HttpSession에 저장
public class ChatController {
	
	@Autowired
	private ChatService chatService;
	
	@GetMapping("/chatRoomList") // 항상 특정 페이지로 포워딩한다
	public String selectChatRoomList(Model model) {
		/*
		 * 업무로직
		 * 0. 권한(인가) 체크 (시큐리티)
		 * 1. 채팅방 목록 조회
		 * 2. 조회된 데이터를 담아 forward
		 * 3. 페이징 처리(생략)
		 */
		List<ChatRoom> list = chatService.selectChatRoomList();
		model.addAttribute("list", list);
		return "chat/chatRoomList";
	}
	
	@PostMapping("/openChatRoom")
	public String openChatRoom(
			@ModelAttribute ChatRoom room,
			RedirectAttributes ra,
			Authentication authentication
			) {
		/*
		 * 1. 유효성 검사(InitBinder, validator 추가) => 생략
		 *  - 채팅방 제목에 부적절한 언어가 포함되지는 않았는지, 길이가 적정한지 등
		 * 2. 테이블에 데이터 추가를 위해 room을 서비스로 전달
		 * 3. 처리결과에 따른 view 페이지 지정
		 */
		Member m = (Member) authentication.getPrincipal();
		room.setUserNo(m.getUserNo());
		int result = chatService.openChatRoom(room);
		if (result == 0) {
			throw new RuntimeException("채팅방 등록 실패");
		}
		ra.addFlashAttribute("alertMsg", "채팅방 생성 성공");
		return "redirect:/chat/chatRoomList";
	}
	
	@GetMapping("/room/{chatRoomNo}")
	public String joinChatRoom(
			@PathVariable("chatRoomNo") int chatRoomNo,
			Model model,
			ChatRoomJoin join,
			Authentication authentication
			) {
		/*
		 * 업무로직
		 * 1. 채팅방 번호를 기준으로 채팅방 메세지 내용 조회 collection list 형태로 데이터 받아올 예정
		 * 2. 참여자수 증가
		 * 3. 채팅방 메세지를 model에 추가 후 포워딩
		 * + 실시간 통신 채널을 열어주어야 함
		 */
		Member loginUser = (Member) authentication.getPrincipal();
		join.setChatRoomNo(chatRoomNo);
		join.setUserNo(loginUser.getUserNo());
		List<ChatMessage> list = chatService.joinChatRoom(join); // 1+2 한번에 처리
		if(list == null) throw new RuntimeException("채팅방 접속 오류");
		model.addAttribute("list", list);
		model.addAttribute("loginUser", loginUser); // 보안정보들 때문에 request에 담음
		model.addAttribute("chatRoomNo", chatRoomNo); // session (SessionAttributes 어노테이션)
		// 채팅설정을 위해서 loginUser, chatROomNo 보관
		// 세션에 담는 이유 : 단발성 데이터가 아니라, 사용자가 채팅방에 머무는 동안 여러기능 (메세지 전송, 접속 유지, 알림 등)
		// 에 계속 필요한 정보기 때문에, 일일이 파라미터로 받는것보다 세션에 저장해두고 재사용
		return "/chat/chatRoom";
	}
}
