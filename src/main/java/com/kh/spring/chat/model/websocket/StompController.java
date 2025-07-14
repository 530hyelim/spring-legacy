package com.kh.spring.chat.model.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kh.spring.chat.model.service.ChatService;
import com.kh.spring.chat.model.vo.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController { // 웹소켓 메세지 매핑 url 관리
	/*
	 * stomp 방식의 메세지 템플릿을 자바로 작성할 수 있도록 도와주는 클래스
	 * 
	 * SimpMessagingTemplate
	 *  - 서버에서 특정 클라이언트에게 메세지를 전송하기 위한 STOMP 템플릿
	 *  - STOMP 구독 경로로 메세지를 전송할 수 있다.
	 *  구독용 url, 전송할 데이터
	 *  convertAndSend : 전체사용자에게 메세지를 보낼 때
	 *  convertAndSendToUser : 특정 사용자에게 메세지를 보낼 때
	 */
	private final SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ChatService service;
	
	/*
	 * @MessageMapping(destination 경로)
	 *  - 클라이언트가 websocket을 통해 지정한 destination 경로를 매핑하는 속성
	 *  
	 * @Payload
	 *  - STOMP의 바디영역의 데이터를 VO 클래스로 바인딩해주는 속성 (JSON -> VO)
	 */
	@MessageMapping("/chat/enter/{roomNo}") // stomp의 destination mapping용 url
	// app 생략 : servlet context에서 app으로 들어오는 요청은 서버가 받는다고 설정해놔서
	public void HandleEnter(
			@DestinationVariable int roomNo,
			@Payload ChatMessage message
			) {
		message.setType(ChatMessage.MessageType.ENTER);
		message.setMessage(message.getUserName()+"님이 입장하셨습니다.");
		// 필요하다면 서비스로직 호출하여 db에 내용 저장
		
		// 브로커에게 메세지 템플릿 전송
		messagingTemplate.convertAndSend("/topic/room/"+roomNo, message);
	}
	
	@MessageMapping("/chat/exit/{roomNo}")
	@SendTo("/topic/room/{roomNo}") // (구독 url 지정) vs. messagingTemplate으로 전달하기 둘 중 하나 선택
	public ChatMessage HandleExit(
			@DestinationVariable int roomNo,
			@Payload ChatMessage message
			) {
		// 1. 참여자 정보 삭제
		service.exitChatRoom(message);
		// 2. 채팅방 참여자 수가 0명이라면 채팅방 삭제
		// 3. 메세지 담은 후 전송
		message.setType(ChatMessage.MessageType.EXIT);
		message.setMessage(message.getUserName()+"님이 퇴장하셨습니다.");
		return message;
	}
	
	// @CrossOrigin : 교처출처 허용. 모든 Origin에 대해서.
	
	// 관리자 공지 메세지용 매핑 url
	@MessageMapping("/notice/send")
	public void sendNotice(@Payload String alertMsg) {
		// 공지 내용을 db에 저장
		// 알림 받게될 사용자 지정 등 기타 업무로직 생략
		messagingTemplate.convertAndSend("/topic/notice", alertMsg);
	}
}
