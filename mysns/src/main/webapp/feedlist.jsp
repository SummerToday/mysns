<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Feed List</title>
<script>
function goToDetail(aid) {
    window.location.href = 'snsController?action=viewFeed&aid=' + aid;
}

function likeFeed(aid) {
    fetch('snsController?action=likeFeed&aid=' + aid, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => {
        if(data === 'success') {
            const likeCountElement = document.getElementById('likeCount_' + aid);
            likeCountElement.innerText = parseInt(likeCountElement.innerText) + 1;
        } else {
            alert('Failed to like the feed.');
        }
    });
}
</script>
</head>
<body>
<%
if(session.getAttribute("login_id") == null) {
    session.setAttribute("login_id", request.getParameter("id"));
}
%>
<table align="center">
    <tr>
        <td colspan="2" align="left" height="40"><b>작성글 리스트</b></td>
        <form method="post" action="/mysns/write.jsp">
        <td colspan="2" align="right" height="40">
            <input type="submit" value="글쓰기"></td>
        </form>
        <form method="post" action="/mysns/snsController?action=logout">
        <td colspan="2" align="right" height="40"><input type="submit" value="로그아웃"></td>
        </form>
        <c:choose>
            <c:when test="${not empty sessionScope.showMyFeeds}">
                <form method="post" action="/mysns/snsController?action=showAllFeeds">
                <td colspan="2" align="right" height="40"><input type="submit" value="전체 글 보기"></td>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="/mysns/snsController?action=myFeeds">
                <td colspan="2" align="right" height="40"><input type="submit" value="내가 쓴 글만 보기"></td>
                </form>
            </c:otherwise>
        </c:choose>
    </tr>
    <c:forEach var="feeds" items="${feedlist}" varStatus="status">
        <tr class="table-row" onclick="goToDetail(${feeds.aid})">
            <td colspan="2" align="left" height="40">${feeds.id} 
                <c:if test="${feeds.is_Private}">
                    <span style="color:red">(비공개)</span>
                </c:if>
            </td>
            <td colspan="2" align="right" height="40">${feeds.created_at}</td>
        </tr>
        <tr class="table-row" onclick="goToDetail(${feeds.aid})">
            <td><img src="snsController?aid=${feeds.aid}" alt="Feed Image"></td>
        </tr>
        <tr class="table-row">
            <td><b>${feeds.content}</b></td>
            <td>
                <c:if test="${feeds.id == sessionScope.user.id}">
                    <a href="snsController?action=delFeeds&aid=${feeds.aid}" onclick="return confirm('Are you sure you want to delete this feed?')">Delete</a>
                </c:if>
            </td>
            <td>
                <button onclick="likeFeed(${feeds.aid})">Like</button>
                <span id="likeCount_${feeds.aid}">${feeds.likeCount}</span>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
