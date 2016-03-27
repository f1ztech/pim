<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="pfn" uri="/WEB-INF/tags/pim.tld"%>
<%@ attribute name="path" type="java.lang.String"%>
<%@ attribute name="title" type="java.lang.String"%>

<c:set var="commandName" value="${requestScope['org.springframework.web.servlet.tags.form.AbstractFormTag.modelAttribute']}"/>
<c:set var="bindingResult" value="${requestScope[pfn:functions().concat('org.springframework.validation.BindingResult.', commandName)]}"/>

<div class="form-group ${not empty bindingResult.getFieldError(path) ? 'has-error' : ''}">
	<label for="${path}"><c:out value="${title}"/></label>
	<sf:input path="${path}" cssClass="form-control" />
	<sf:errors path="${path}" cssClass="control-label" element="label" />
</div>