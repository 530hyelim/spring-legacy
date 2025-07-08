<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>제목</title>
<!--  공통적으로사용할 라이브러리 추가 -->
<!-- Jquery 라이브러리 -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<!-- 부트스트랩에서 제공하있는 스타일 -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
<!-- 부트스트랩에서 제공하고있는 스크립트 -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

<!-- alertify -->
<script src="//cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/alertify.min.js"></script>
<!-- alertify css -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/css/alertify.min.css"/>
<!-- Default theme -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/css/themes/default.min.css"/>
<!-- Semantic UI theme -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/css/themes/semantic.min.css"/>
<style>
	div {box-sizing:border-box;}
#header {
	width: 80%;
	height: 130px;
	padding-top:20px;
	margin :auto;
	
	/* 추가 */
	position: sticky;
    top: 0; /* 최상단에 붙임*/
    background-color: white;
    border-bottom : 2px solid black;
    z-index: 10;
}
#header>div{width:100%; margin-bottom: 10px;}
#header_1 {height : 40%;}
#header_2 {height : 60%;}

#header_1>div{
	height:100%;
	float:left;
}
#header_1_left {width :30%; position : relative;}
#header_1_center{width: 40%;}
#header_1_right{width : 30%;}

#header_1_left>img{
height:80%; position:absolute; margin:auto; top:0px; bottom:0px; right:0px; left :0px;
}
#header_1_right {text-align:center; line-height: 35px; font-size: 12px; text-indent:35px;}
#header_1_right>a{margin:5px;}
#header_1_right>a:hover{cursor:pointer}

#header_2>ul{width:100%; height:100%; list-style-type : none; margin:auto; padding: 0; 
    display: flex; /* 추가 */

}
#header_2>ul>li {float:left; width: 25%; height:100%; line-height: 55px; text-align:center;}
#header_2>ul>li a {font-size: 18px; font-weight:900}

#header_2 {border-top : 1px solid lightgray;}

#header a {text-decoration:none; color:black;}

/* 세부페이지에 들어갈 공통 css 부여*/
.content{
	background-color: pink;
	width:80%;
	margin:auto;
}
.innerOuter{
	border:1px solid lightgray;
	width: 80%;
	margin:auto;
	padding: 5% 10%;
	background-color : white;
}
</style>
</head>
<body>
<c:if test="${not empty alertMsg}">
	<script>
		alertify.alert("서비스 요청 성공", "${alertMsg}");
		// alertify? 추가한 라이브러리
		// 윈도우의 alert, confirm과 같은 메서드의 ui를 커스터마이징 할 수 있음
	</script>
	<!-- <c:remove var="alertMsg"/> -->
	<!-- 안지워주면 페이지 리프레시 할때마다 계속 뜸 -->
</c:if>

<c:set var="contextPath" value="${pageContext.request.contextPath}" scope="application"/>
<!-- classpath는 뭐였지? 자바 클래스 파일들이 컴파일 완료되면 담기는 곳 (context module에 있음) -->
	<div id="header">
		<div id="header_1">
			<div id="header_1_left">
				<img src="https://www.iei.or.kr/resources/images/main/main_renewal/top_logo.jpg" />
			</div>
			<div id="header_1_center">
			</div>
			<c:set var="principal" value="${pageContext.request.userPrincipal}"/>
			<!-- principal : 인증된 사용자 정보가 담겨있음 (MemberExt) -->
			<div id="header_1_right">
				<!-- 로그인하지 않은 사용자가 보이는 화면 -->
				<sec:authorize access="isAnonymous()">
					<a href="${contextPath }/security/insert">회원가입</a>
					<a href="${contextPath }/member/login">로그인</a>
				</sec:authorize>
				
				<sec:authorize access="isAuthenticated()">
					<span><sec:authentication property="principal.username"/>님 환영합니다 ^^</span>
					<!-- 로컬상에 아파치 톰캣을 깔아서 사용할 경우 이전 세션의 정보를 가져오려고 하기때문에
					서버재시작하고 새로고침하면 null로 나옴 -->
                       <a href="${contextPath}/security/myPage"
                           class="text-decoration-none text-secondary">마이페이지</a>
                       <form:form action="${contextPath}/member/logout" method="post" style="display:inline;">
                           <button type="submit"
                                   class="border-0 bg-transparent text-secondary p-0 ml-2"
                                   style="cursor:pointer;">
                               로그아웃
                           </button>
                       </form:form>
                       <!-- csrf 토큰이 필요하기 때문에 get방식으로 못보내고 post로 보내야함 폼폼태그 사용해서 -->
				</sec:authorize>
			</div>
		</div>
		<div id="header_2">
			<ul>
				<!-- 권한별 노출 url 설정 -->
				<!-- authentication으로 username이 있는지 등을 확인할수있지만 이 방법이 가장 권장됨 -->
				<sec:authorize access="hasRole('ROLE_USER')">
					<li><a href="${contextPath }">HOME</a></li>
					<li><a href="${contextPath }/chat/chatRoomList">채팅</a></li>
					
					<c:forEach var="map" items="${boardTypeMap}">
						<!-- list 형태로 저장해도 되는데 반복문 돌려서 안하고 한번에 빠르게 원하는 데이터를 찾아가게 하려고 -->
						<li><a href="${contextPath }/board/list/${map.key}">${map.value.boardName}</a></li>
					</c:forEach>
				</sec:authorize>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')">
					<li><a>관리자</a></li>
				</sec:authorize>
			</ul>
		</div>	
	</div>



</body>
</html>