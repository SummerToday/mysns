<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script>
function goToDetail(aid) {
    window.location.href = 'snsController?action=viewFeed&aid=' + aid;
}
</script>
</head>
<body>
	<%
	if(session.getAttribute("login_id")==null)
		session.setAttribute("login_id", request.getParameter("id"));
	%>
	<table align=center>
		<tr>
			<td colspan=2 align=left height=40><b>작성글 리스트</b></td>
			<form method="post" action="/mysns/write.jsp">
			<td colspan=2 align=right height=40>
				<input type="submit" value="글쓰기"></td>
			</form>
			<form method="post" action="/mysns/snsController?action=logout">
			<td colspan=2 align=right height=40><input type="submit"
				value="로그아웃"></td>
			</form>
		</tr>
		 <c:forEach var="feeds" items="${feedlist}" varStatus="status">
            <tr class="table-row" onclick="goToDetail(${feeds.aid})">
                <td colspan="2" align="left" height="40">${feeds.id}</td>
                <td colspan="2" align="right" height="40">${feeds.created_at}</td>
            </tr>
            <tr class="table-row" onclick="goToDetail(${feeds.aid})">
                <td><img src="snsController?aid=${feeds.aid}"></td>
            </tr>
            <tr class="table-row" onclick="goToDetail(${feeds.aid})">
                <td><b>${feeds.content}</b></td>
                <td><a href="snsController?action=delFeeds&aid=${feeds.aid}"><span class="badge bg-secondary"> &times; </span></a></td>
            </tr>
        </c:forEach>
	</table>
</body>
</html>