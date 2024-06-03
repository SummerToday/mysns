<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>게시글 상세 보기</title>
</head>
<body>
	<h1>게시글 상세 보기</h1>
	<c:choose>
		<c:when test="${not empty feed}">
			<table border="1" align="center">
				<tr>
					<td><b>작성자:</b></td>
					<td>${feed.id}</td>
				</tr>
				<tr>
					<td><b>작성일:</b></td>
					<td>${feed.created_at}</td>
				</tr>
				<tr>
					<td><b>내용:</b></td>
					<td>${feed.content}</td>
				</tr>
				<tr>
					<td colspan="2"><img src="snsController?aid=${feed.aid}"></td>
				</tr>
			</table>
			<br>
			<form action="/mysns/snsController" method="get">
				<input type="hidden" name="action" value="editFeed"> <input
					type="hidden" name="aid" value="${feed.aid}"> <input
					type="submit" value="수정하기">
			</form>
			<br>
			<form action="/mysns/snsController" method="post">
				<input type="submit" value="목록으로 돌아가기">
			</form>

		</c:when>
		<c:otherwise>
			<p>선택된 게시글이 없습니다.</p>
		</c:otherwise>
	</c:choose>
</body>
</html>
