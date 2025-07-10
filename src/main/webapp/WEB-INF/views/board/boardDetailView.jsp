<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<style>
	img{
		width:400px;
	}
</style>
</head>
<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp"></jsp:include>
	
	<div class="content">
      <br><br>
      <div class="innerOuter">
         <h2>게시글 상세보기</h2>
         <br>
         <table id="contentArea" align="center" class="table">
            <tr>
               <th width="100">제목</th>
               <td colspan="3">${board.boardTitle}</td>
            </tr>
            <tr>
               <th>작성자</th>
               <td>
                  ${board.userName }
               </td>
               <th>작성일</th>
               <td>${board.createDate }</td>
            </tr>
            <c:set var="imgList" value="${board.imgList}" />
            <c:if test="${not empty imgList }">
               <c:choose>
                  <c:when test="${boardCode == 'P'}">
                     <c:forEach var="i" begin="0" end="${fn:length(imgList) - 1}">
                        <tr>
                           <th>이미지${i+1 }</th>
                           <td colspan="3">
                              <a href="${contextPath }${imgList[i].changeName}"
                              download="${imgList[i].originName }">
                              <!-- 같은 출처(same-origin)의 URL이거나, 서버가 Content-Disposition : attachment
                              헤더를 포함한 경우, 사용자가 링크를 클릭할 때 브라우저에서 열리지 않고 바로 다운로드 됨
                              크로스 도메인 (출처가 다른 경우) 파일에는 일부 브라우저에서 제한이 있을 수 있음 
                              따라서 웹 경로상 (서버에 업로드된 URL 경로)에 있어야 접근이 가능하고, db같은데에 저장되는 경우
                              LOB같은 데이터를 꺼내서 브라우저로 이미지 파일 형태로 반환해야 함 (이과정에서 stream 필요할지도) 
                              -->
                              <img src="${contextPath }${imgList[i].changeName}">
                              </a>
                           </td>
                        </tr>
                     </c:forEach>
                  </c:when>
                  <c:otherwise>
                     <tr>
                        <th>첨부파일</th>
                        <td>
                           <button type="button" class="btn btn-outline-success btn-block"
                           onclick="location.href='${contextPath}/board/fileDownload/${board.boardNo }'">
                              ${imgList[0].originName } - 다운로드
                           </button>
                        </td>
                     </tr>
                  </c:otherwise>
               </c:choose>
            </c:if>
            <tr>
               <th>내용</th>
               <td></td>
               <th>조회수</th>
               <td>${board.count }</td>
            </tr>
            <tr>
               <td colspan="4">
                  <p style="height:150px;">
                     ${board.boardContent}
                  </p>
               </td>
            </tr>
         </table>
         <br>
         <!-- Spring EL. 자료형이 다른데 ==하면 무조건 false 나옴(int vs. string) -->
         <sec:authorize access="hasRole('ROLE_ADMIN') or principal.userNo.toString() == #board.boardWriter"> 
	         <div align="center">
	            <a class="btn btn-primary" href="${contextPath }/board/update/${boardCode}/${board.boardNo}">수정하기</a>
	         </div>
         </sec:authorize>
      </div>
   </div>
	
	<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>
</body>
</html>