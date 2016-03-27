<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags/form"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ page session="false" pageEncoding="UTF-8"%>

<l:main-layout>
	<div class="container register">
		<h1>Регистрация</h1>
		<sf:form commandName="user">
			<f:input path="login" title="Логин"/>
			<f:password path="password" title="Пароль"/>
			<button type="submit" class="btn btn-primary">Зарегистрироваться</button>
		</sf:form>
	</div>
</l:main-layout>