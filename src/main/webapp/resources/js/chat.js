// IIFE 즉시실행함수. 다른곳에서 절대 접근 못해서 캡슐화/모듈화에 좋음
(function(){
    const display = document.querySelector(".display-chatting");
    // 채팅창 맨 아래로 내리기
    display.scrollTop = display.scrollHeight;
})();

// 채팅메세지 보내기 기능
document.getElementById("send").addEventListener('click', sendMessage);

function sendMessage() {
    // 채팅 메세지
    const input = document.getElementById("inputChatting");

    if(input.value.trim().length == 0) {
        alert("1글자 이상 작성하세요");
        input.value = "";
        input.focus();
        return;
    }

    const chatMessage = {
        message : input.value,
        chatRoomNo , // 속성 이름과 속성값의 이름이 일치할 경우 생략 가능
        userNo, // JSP에서 선언한 전역변수
        userName
    };

    const json = JSON.stringify(chatMessage); // js -> json
    // json -> js : parse
    chattingSocket.send(json); // 웹소켓을 통해 데이터를 전송하는 함수
    input.value = "";
}

// 서버에서 전달(푸쉬)하는 메세지를 실시간으로 감지하는 이벤트 핸들러
chattingSocket.onmessage = function(e) {
    // 서버에서 전달한 json 데이터를 파싱
    const chatMessage = JSON.parse(e.data);

    const li = document.createElement("li");
    const p = document.createElement("p");
    p.classList.add("chat");

    p.innerHTML = chatMessage.message.replace(/\\n/gm, "<br>");

    const span = document.createElement("span");
    span.classList.add("chatDate");
    span.innerText = chatMessage.createDate;

    if (chatMessage.userNo == userNo) {
        li.classList.add("myChat");
        li.append(span, p);
    } else {
        li.innerHTML = `<b>${chatMessage.userName}</b>`;
        li.append(p, span);
    }

    const display = document.querySelector(".display-chatting");
    display.append(li);
    display.scrollTop = display.scrollHeight;
};