<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
    <table align=center>
        <tr>
            <td colspan=2 align=center height=40><b> 글작성</b></td>
        </tr>
        <form method="post" action="/mysns/snsController?action=write" enctype="multipart/form-data">
        <tr>
            <td align=center>작성글</td>
            <td>
                <textarea name="content" cols="40" rows="6"></textarea><br>
            </td>
        </tr>
        <tr>
            <td align=left>이미지</td>
            <td><input type="file" name="image"></td>
        </tr>
        <tr>
            <td align=left>비공개</td>
            <td><input type="checkbox" name="private" value="true"></td>
        </tr>
        <tr>
        <td colspan=2 align=center height=40>
        <input type="submit" value="업로드하기"></td>
        <tr>
        </form>
    </table>
</body>
</html>
