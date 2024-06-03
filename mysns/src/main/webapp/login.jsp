<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
    // 쿠키 확인
    Cookie[] cookies = request.getCookies();
    boolean loggedIn = false;

    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("login_id")) {
                loggedIn = true;
                break;
            }
        }
    }

    // 로그인 상태라면 메인 페이지로 리디렉션
    if (loggedIn) {
        response.sendRedirect("/mysns/snsController");
        return;
    }
%>

    <table align=center>
        <tr>
            <td colspan=2 align=center height=40><b>로그인</b></td>
        </tr>
        <form method="post" action="/mysns/snsController?action=login">
        <tr>
            <td align=right>아이디&nbsp;</td>
            <td><input type="text" name="id" placeholder="Email address" required></td>
        </tr>
        <tr>
            <td align=right>패스워드&nbsp;</td>
            <td><input type="password" name="password" required></td>
        </tr>
        <tr>
            <td align=right>자동 로그인&nbsp;</td>
            <td><input type="checkbox" name="rememberMe" value="true"></td>
        </tr>
        <tr>
            <td colspan=2 align=center height=50>
            <input type="submit" value="로그인하기"></td>
        </tr>
        </form>
        <tr>
            <td colspan=2 align=center><small><br><br>아직 회원이 아니세요?<br><br>
            <a href="signup.jsp">회원가입</a></small></td>
        </tr>
    </table>

</body>
</html>
