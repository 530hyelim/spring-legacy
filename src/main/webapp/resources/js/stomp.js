/*
    stomp 동작방식
    1. 클라이언트가 SockJS (웹소켓)을 이용하여 서버에 "연결요청"을 보냄
     - 연결 수락 시 서버는 Connected 프레임을 웹소켓을 통해 반환
    2. 클라이언트는 서버와 연결 수립 후 구독중인 url을 전달
*/
// 스톰프 클라이언트로 연결이 완료된 후 실행할 핸들러
stompClient.connect({},function (e){ // 1. WebSocket + STOMP 연결 완료됨
    console.log(e);
    // 현재 클라이언트가 구독중인 url 목록들을 전달
    // 현재 채팅방에 새로운 사용자가 입장하거나, 사용자가 퇴장하는 경우의
    // 구독 url
    // 2. 서버로부터 메세지를 받을 채널에 구독 시작
    stompClient.subscribe("/topic/room/"+chatRoomNo, function(message) {
        // chat.js의 onmessage와 비슷하다 (url단위로 지정할 수 있다는 특징)
        // message.body가 본문
        // 서버에서 브로드캐스트한 메세지를 수신
        const chatMessage = JSON.parse(message.body);
        showMessage(chatMessage);
    });
    // 입장메세지 서버로 전송 (destination, header, body)
    // 3. 서버로 입장 메세지를 전송
    stompClient.send("/app/chat/enter/"+chatRoomNo, {}, JSON.stringify({
        userName, // 객체의 속성명과 변수명이 동일한 경우 생략가능
        chatRoomNo,
        userNo
    }));
    // 4. 서버는 메세지를 처리하고 응답을 브로드캐스트
});
function showMessage(message) {
    const li = document.createElement("li");
    const p = document.createElement("p");
    p.classList.add("chat");
    p.innerText = message.message;
    p.style.color = "gray";
    p.style.textAlign = "center";
    li.append(p);
    document.querySelector(".display-chatting").append(li);
}
/*
    - subscribe와 send 메서드는 둘 다 비동기
    - send에 대한 응답 메세지를 받기 전까지 subscribe에 등록된 콜백함수는
      실행되지 않음
    - 즉, subscribe()는 먼저 실행되지만, showMessage()는 서버에서 메세지를
      보낸 후에 실행됨
*/
const exitBtn = document.querySelector("#exit-btn");
exitBtn.onclick = function() {
    // 서버로 퇴장 메세지 전송하기
    // 1. CHAT_ROOM_JOIN에서 한 행의 데이터 삭제
    // 2. 현재 채팅방에 참여자가 0명이라면 채팅방 삭제 (db와 직접 연결)
    // 3. 같은 방을 이용하는 모든 사용자에게 알림내용 전송 (stomp 방식)
    stompClient.send("/app/chat/exit/"+chatRoomNo, {}, JSON.stringify({
        userName : userName,
        chatRoomNo : chatRoomNo, // vo 클래스로 자동매핑하기 위해서
        userNo : userNo
    }));
    stompClient.disconnect(function() {
        location.href = `${contextPath}/chat/chatRoomList`;
        // 페이지이동하면 자동으로 연결해제되지만 시간이 오래걸려서 직접해제
    });
};