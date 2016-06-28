<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="pfn" uri="pimTagLib"%>

<%@ attribute name="showSearch" type="java.lang.Object"%>

<c:set var="functions" scope="request" value="${pfn:functions()}"/>

<!DOCTYPE html>
<html ng-app="pimApp">
<head>
	<meta charset="utf-8"/>
	<meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1, maximum-scale=1">
	<title>Система семантического управления личной информацией</title>
    <link rel="stylesheet" href="/resources/styles/kendo/kendo.common.min.css" />
    <link rel="stylesheet" href="/resources/styles/kendo/kendo.metro.min.css" />
    <link rel="stylesheet" href="/resources/styles/kendo/kendo.dataviz.min.css" />
    <link rel="stylesheet" href="/resources/styles/kendo/kendo.dataviz.metro.min.css" />
	<link rel="stylesheet" href="/resources/styles/bootstrap.css" />
	<link rel="stylesheet" href="/resources/styles/pim.css" />
	<link rel="stylesheet" href="/resources/styles/mapjs.css" />
	<link rel="stylesheet" href="/resources/styles/font-awesome/font-awesome.min.css" />

	<script src="/resources/scripts/lib/jquery-2.1.1.min.js"> </script>
	<script src="/resources/scripts/lib/jquery.debounce-1.0.5.js"> </script>

	<script src="/resources/scripts/lib/bootstrap.min.js"> </script>
	<script src="/resources/scripts/lib/angular-1.2.17.js"> </script>
	<script src="/resources/scripts/lib/angular-ui-router.js"> </script>
	<script src="/resources/scripts/lib/angular-resource-1.2.17.js"> </script>
	<script src="/resources/scripts/lib/angular-sanitize.js"></script>
	<script src="/resources/scripts/lib/kendo.all.min.js"></script>

	<script src="/resources/scripts/mapjs/lib/jquery.hotkeys.js"></script>
	<script src="/resources/scripts/mapjs/lib/hammer.min.js"></script>
	<script src="/resources/scripts/mapjs/lib/jquery.hammer.min.js"></script>
	<script src="/resources/scripts/mapjs/lib/underscore-1.4.4.js"></script>
	<script src="/resources/scripts/mapjs/lib/color-0.4.1.min.js"></script>
	<script src="/resources/scripts/mapjs/mindmup-mapjs.js"></script>

	<script src="/resources/scripts/mapjs/observable.js"></script>
	<script src="/resources/scripts/mapjs/mapjs.js"></script>
	<script src="/resources/scripts/mapjs/url-helper.js"></script>
	<script src="/resources/scripts/mapjs/content.js"></script>
	<script src="/resources/scripts/mapjs/layout.js"></script>
	<script src="/resources/scripts/mapjs/clipboard.js"></script>
	<script src="/resources/scripts/mapjs/map-model.js"></script>
<!-- 	<script src="/resources/scripts/mapjs/drag-and-drop.js"></script> -->
	<script src="/resources/scripts/mapjs/map-toolbar-widget.js"></script>
	<script src="/resources/scripts/mapjs/link-edit-widget.js"></script>
	<script src="/resources/scripts/mapjs/image-drop-widget.js"></script>
	<script src="/resources/scripts/mapjs/hammer-draggable.js"></script>
	<script src="/resources/scripts/mapjs/dom-map-view.js"></script>
	<script src="/resources/scripts/mapjs/dom-map-widget.js"></script>

	<script src="/resources/scripts/app.js"> </script>
	<script src="/resources/scripts/common.js"> </script>
	<script src="/resources/scripts/controllers/workbench-controllers.js"> </script>
	<script src="/resources/scripts/services/workbench-service.js"> </script>
	<script src="/resources/scripts/controllers/mindmap-controllers.js"> </script>
	<script src="/resources/scripts/controllers/search-controller.js"> </script>
	<script src="/resources/scripts/controllers/mail-controller.js"> </script>
</head>
<body>
  <div class="body">
	<header class="pim-header">
		<div class="pim-header-title">
			<h2><a href="/">Система управления личной информацией</a></h2>
		</div>
		<div class="pim-login">
			<c:choose><c:when test="${not functions.loggedIn}">
				<a class="btn btn-link" href="/user/register">Регистрация</a>
				<button type="button" class="btn btn-primary pim-login-button" data-placement="bottom" data-animation="true">
				  Авторизация
				</button>
				<div id="login-content" style="display: none;">
					<form role="form" action="j_spring_security_check" method="post">
						<input class="form-control input-sm login-field" name="j_username" placeholder="Логин">
						<input class="form-control input-sm login-field" name="j_password" placeholder="Пароль" type="password">
						<button type="submit" class="btn btn-default btn-sm login-submit">Войти</button>
					</form>
				</div>
			</c:when><c:otherwise>
				<span class="login-context">
					<c:out value="${functions.currentUser.login}"/>
					<a href="/logout">Выход</a>
				</span>
			</c:otherwise> </c:choose>
		</div>
	</header>
	<nav class="navbar navbar-default pim-top-menu" role="navigation">
		<div class="container-fluid">

			<div class="navbar-header">
				<a class="navbar-brand glyphicon glyphicon-home pim-home" href="#"> </a>
			</div>

			<div class="collapse navbar-collapse">
				<c:if test="${functions.loggedIn}">
					<ul class="nav navbar-nav">
						<li class="active"><a href="#/workbench">Рабочий стол</a></li>
<!-- 						<li><a href="#">Файлы</a></li> -->
						<li><a href="#/mail">Почта</a></li>
<!-- 						<li><a href="#">События</a></li> -->
					</ul>
					<c:if test="${showSearch != false}">
						<form class="navbar-form navbar-right top-search-form" role="search" ng-controller="SearchCtrl">
							<div class="form-group">
								<input type="text" class="form-control top-search" placeholder="Поиск" ng-model="searchText" ng-change="search()">
								<div class="top-search-results" ng-if="results !== null || loading">
									<div ng-repeat="resource in results" ng-click="navigateToResource(resource)">
										<i ng-class="resource.icon" style="top: 2px;"></i> {{resource.title}}
									</div>
									<div ng-if="!loading && hasNextPage()" ng-click="nextPage()">Показать еще {{pageSize}} результатов</div>
									<div ng-if="!loading && !results.length">Ничего не найдено</div>
									<div ng-if="loading">Загрузка...</div>
								</div>
							</div>
						</form>
					</c:if>
				</c:if>
			</div>
		</div>
	</nav>
	<div class="pim-container">
		<jsp:doBody/>
	</div>
  </div>
</body>
</html>
