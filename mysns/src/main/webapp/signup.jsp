<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입</title>
<script>
function validateForm() {
    var id = document.forms["signupForm"]["id"].value;
    var password = document.forms["signupForm"]["password"].value;
    var name = document.forms["signupForm"]["name"].value;

    if (id.length < 4 || id.length > 16) {
        alert("아이디는 4자에서 16자 이내로 입력해주세요.");
        return false;
    }

    if (password.length < 8 || !/^\d+$/.test(password)) {
        alert("패스워드는 8자 이상이고 숫자로만 입력해주세요.");
        return false;
    }

    if (!/^[A-Z]+$/.test(name)) {
        alert("이름은 영문 대문자로만 입력해주세요.");
        return false;
    }

    if (/^\d/.test(name)) {
        alert("이름은 숫자로 시작할 수 없습니다.");
        return false;
    }

    return true;
}
</script>
</head>
<body>
    <table align="center">
        <tr>
            <td colspan="2" align="center" height="40"><b>회원가입</b></td>
        </tr>
        <form name="signupForm" method="post" action="/mysns/snsController?action=signup" onsubmit="return validateForm()">
        <tr>
            <td align="right">아이디&nbsp;</td>
            <td><input type="text" name="id" placeholder="4~16자 이내의 아이디" required></td>
        </tr>
        <tr>
            <td align="right">패스워드&nbsp;</td>
            <td><input type="password" name="password" placeholder="8자 이상의 숫자 패스워드" required></td>
        </tr>
        <tr>
            <td align="right">이름&nbsp;</td>
            <td><input type="text" name="name" placeholder="영문 대문자로만 입력" required></td>
        </tr>
        <tr>
            <td colspan="2" align="center" height="50"><input type="submit" value="회원가입하기"></td>
        </tr>
        </form>
    </table>
</body>
</html>
