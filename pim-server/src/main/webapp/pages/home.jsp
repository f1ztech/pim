<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"%>
<%@ page session="false" pageEncoding="UTF-8"%>

<l:main-layout>
	<div class="pim-wrapper" ui-view> </div>
	<div class="alert alert-success" id="pim-success">
	    <button type="button" class="close" data-dismiss="alert">&times;</button>
	    <div class="alert-text"></div>
	</div>
	<div class="alert alert-danger" id="pim-failure">
	    <button type="button" class="close" data-dismiss="alert">&times;</button>
	    <div class="alert-text"></div>
	</div>
</l:main-layout>