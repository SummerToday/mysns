<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>게시글 수정</title>
</head>
<body>
    <h1>게시글 수정</h1>
    <form action="/mysns/snsController" method="post" enctype="multipart/form-data">
        <input type="hidden" name="action" value="updateFeed">
        <input type="hidden" name="aid" value="${feed.aid}">
        <label>작성자:</label><br>
        <input type="text" name="id" value="${feed.id}" disabled><br>
        <label>내용:</label><br>
        <textarea name="content">${feed.content}</textarea><br>
        <label>이미지:</label><br>
        <input type="file" name="uploadFile"><br> <!-- 파일 업로드 필드 -->
        <input type="submit" value="수정 완료">
    </form>
    <br>
    <form action="/mysns/snsController" method="post">
        <input type="submit" value="취소">
    </form>
</body>
</html>
