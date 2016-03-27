<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"%>
<%@ page session="false" pageEncoding="UTF-8"%>

<l:main-layout showSearch="false">

	<script>
	    $(document).ready(function() {
	        $(".pim-tree").kendoTreeView();
	    });
	</script>	
	
	
	<div class="pim-content">
		<div class="pim-left">
			<h4>Теги</h4>
	        <ul id="treeview" class="pim-tree">
	            <li data-expanded="true">
	                 <i class="glyphicon glyphicon-tag"></i>
	                PIM
	                <ul>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Publication Management
	                    </li>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Knowledge Management
	                    </li>
	                    <li><i class="glyphicon glyphicon-tag"></i>Semantic Desktop</li>
	                    <li><i class="glyphicon glyphicon-tag"></i>Обзоры</li>
	                </ul>
	            </li>                
	            <li data-expanded="true">
	                <i class="glyphicon glyphicon-tag"></i>
	                To see
	                <ul>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>PIM
	                    </li>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Knowledge Management
	                    </li>
	                    <li><i class="glyphicon glyphicon-tag"></i>TIM</li>
	                </ul>
	            </li>
	        </ul>
		</div>
		<div class="pim-main">
	        <ul id="project-tree" class="pim-tree">
	            <li data-expanded="true">
	                <span class="glyphicon glyphicon-folder-open"></span>
	                PIM
	                <ul>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Spreading Activation<ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul>
	                    </li>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Knowledge Management<ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul>
	                    </li>
	                    <li>
	                    	<i class="glyphicon glyphicon-tag"></i>Semantic Desktop
			                <ul>
			                    <li data-expanded="true">
			                        <i class="glyphicon glyphicon-book"></i>Towards a Conceptual Framework and Metamodel for Context-aware Personal Cross-Media Information Management Systems
			                    </li>
			                    <li data-expanded="true">
			                        <i class="glyphicon glyphicon-envelope"></i>Re: Обсуждение статьи
			                    </li>
			                    <li><i class="glyphicon glyphicon-file"></i>Semantic Desktop</li>
			                    <li>
			                    	<i class="glyphicon glyphicon-tag"></i>Реализации
					                <ul>
					                    <li data-expanded="true">
					                        <i class="glyphicon glyphicon-book"></i>Haystack. A General-Purpose Information Management Tool for End Users Based on Semistructured Data 
					                    </li>
					                    <li data-expanded="true">
					                        <i class="glyphicon glyphicon-book"></i>OurSpaces – Design and Deployment of a Semantic Virtual Research Environment
					                    </li>
					                    <li><i class="glyphicon glyphicon-book"></i>IRIS. Integrate. Relate. Infer. Share</li>
					                    <li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li>
					                </ul>	                    	
			                    </li>
			                </ul>	                    	
	                    </li>
	                    <li>
	                    	<i class="glyphicon glyphicon-tag"></i>Обзоры
			                <ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul>		                    	
	                    </li>
	                </ul>
	            </li>                
	            <li data-expanded="true">
	                <i class="glyphicon glyphicon-folder-open"></i>
	                Publication Management System
	                <ul>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Docear<ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul>
	                    </li>
	                    <li data-expanded="true">
	                        <i class="glyphicon glyphicon-tag"></i>Public<ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul>
	                    </li>
	                    <li><i class="glyphicon glyphicon-tag"></i>TIM<ul><li><i class="glyphicon glyphicon-book"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</li></ul></li>
	                </ul>
	            </li>
	        </ul>
		</div>
		<div class="pim-right">
			<div class="pim-panel">
				<div class="panel-heading">Схожие статьи</div>
				<div class="panel-body pim-related">
					<div><a href="#" class="scholar">Building a semantic representation for personal information</a></div>
					<div><a href="#" class="scholar">Integrating memory context into personal information re-finding</a></div>
					<div><a href="#" class="scholar">Information re-finding by context: a brain memory inspired approach</a></div>
					<div><a href="#" class="scholar">Search your memory!-an associative memory based desktop search system</a></div>
					<div class="more"><a href="#">Больше...</a></div>
				</div>
			</div>		
			
			<div class="pim-panel">
				<div class="panel-heading">Свойства</div>
				<div class="panel-body pim-details">
					<div>Наименование</div>
					<div><textarea class="form-control">iMecho: An Associative Memory Based Desktop Search System</textarea></div>
					<div>Авторы</div>
					<div><a class="scholar" href="#">W Wu</a>, Jidong Chen, Hang Guo</div>
					<div>Год</div>
					<div>2009</div>
					<div>Дата загрузки</div>
					<div>03.02.2014 15:37</div>			
					<div>Размер</div>
					<div>893 КБ</div>
					<div class="more"><a href="#">Больше...</a></div>
				</div>
			</div>
		</div>
	</div>
</l:main-layout>