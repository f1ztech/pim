<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"%>
<%@ page session="false" pageEncoding="UTF-8"%>

<l:main-layout showSearch="false">

	<div style="display:none" id="popover-content">
		<div class="pim-list-item small">
			<span class="pim-draggable"> </span>
			<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Personal Knowledge Management with the Social Semantic Desktop</span>
		</div>			
		<div class="pim-list-item small">
			<span class="pim-draggable"> </span>
			<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Why Personal Information Management (PIM) Technologies Are Not Widespread</span>
		</div>
		<div class="pim-list-item small">
			<span class="pim-draggable"> </span>
			<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
		</div>					
		<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>			
	</div>
	
	<div class="row">
		<div class="col-md-12">
			<form class="navbar-form db-search" role="search">
				<div class="form-group">
					<i class="glyphicon glyphicon-search"></i>
					<input type="text" class="form-control" placeholder="Поиск"/>
				</div>
			</form>		
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-12">
			<div class="panel panel-default db-panel">
				<div class="panel-heading">
					<h3 class="panel-title">
						<i class="glyphicon glyphicon-fire"></i> Актуальные
					</h3>
				</div>
				<div class="panel-body">
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-calendar"></i>Конференция МФТИ</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Re: Статья RCDL</span>
					</div>									
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>PIM</span>
					</div>													
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Building a Semantic Representation for Personal Information (LiFiDeA)</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The Gnowsis Semantic Desktop approach to Personal Information Management</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>Spreading Activation</span>
					</div>					
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>TIM</span>
					</div>					
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Обсуждение презентации</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Personal Knowledge Management with the Social Semantic Desktop</span>
					</div>			
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Why Personal Information Management (PIM) Technologies Are Not Widespread</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Understanding the Evolution of Users’ Personal Information Management Practices</span>
					</div>					
					<i class="glyphicon glyphicon-plus-sign db-add"> </i>
<!-- 					<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>							 -->
				</div>
			</div>		
		</div>
	</div>

	<div class="row">
		<div class="col-md-6">
			<div class="panel panel-default db-panel">
				<div class="panel-heading">
					<h3 class="panel-title">
						<i class="glyphicon glyphicon-time"></i> Недавние
					</h3>
				</div>
				<div class="panel-body">
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>Spreading Activation</span>
					</div>				
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The Gnowsis Semantic Desktop approach to Personal Information Management</span>
					</div>				
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Обсуждение презентации</span>
					</div>					
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>OurSpaces – Design and Deployment of a Semantic Virtual Research Environment</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>PIM</span>
					</div>									
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>Semantic Desktop</span>
					</div>						
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>DeepaMehta — A Semantic Desktop</span>
					</div>			
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-calendar"></i>Конференция RCDL</span>
					</div>					
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Why Personal Information Management (PIM) Technologies Are Not Widespread</span>
					</div>
					<i class="glyphicon glyphicon-plus-sign db-add"> </i>
<!-- 					<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>					 -->
				</div>
			</div>		
		</div>
		<div class="col-md-6">
			<div class="panel panel-default db-panel">
				<div class="panel-heading">
					<h3 class="panel-title">
						<i class="glyphicon glyphicon-star"></i> Популярные
					</h3>
				</div>
				<div class="panel-body">
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>PIM</span>
					</div>														
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The Gnowsis Semantic Desktop approach to Personal Information Management</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-tag"></i>Semantic Desktop</span>
					</div>					
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Тема диссертации</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
					</div>
					<div class="pim-list-item small">
						<span class="pim-draggable"> </span>
						<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Feldspar. A System for Finding Information by Association</span>
					</div>
					<i class="glyphicon glyphicon-plus-sign db-add"> </i>
<!-- 					<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div> -->
				</div>
			</div>			
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-12 db-watched">
			<div class="panel panel-default db-panel">
				<div class="panel-heading">
					<h3 class="panel-title">
						<i class="glyphicon glyphicon-eye-open"></i> Наблюдение
						<span class="db-add-watched"><i class="glyphicon glyphicon-plus-sign"> </i> <span>Добавить понятие</span></span>
					</h3>
				</div>
				<div class="panel-body">
					<div class="row db-watched-subjects">
						<div class="col-md-4">
							Personal Information Management
						</div>
						<div class="col-md-4">
							Semantic Desktop
						</div>
						<div class="col-md-4">
							Spreading Activation
						</div>
					</div>		
					<div class="row db-watched-body">
						<div class="col-md-4">
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Тема диссертации</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Feldspar. A System for Finding Information by Association</span>
							</div>			
							<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>
						</div>
						<div class="col-md-4">
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The Gnowsis Semantic Desktop approach to Personal Information Management</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>DeepaMehta — A Semantic Desktop</span>
							</div>					
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The NEPOMUK Project - On the way to the Social Semantic Desktop</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Feldspar. A System for Finding Information by Association</span>
							</div>			
							<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>
						</div>
						<div class="col-md-4">
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>The Gnowsis Semantic Desktop approach to Personal Information Management</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Why Personal Information Management (PIM) Technologies Are Not Widespread</span>
							</div>					
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-envelope"></i>Тема диссертации</span>
							</div>
							<div class="pim-list-item small">
								<span class="pim-draggable"> </span>
								<span class="pim-title"><i class="glyphicon glyphicon-file"></i>Desktop Gateway. Semantic Desktop Integration with Cloud Services</span>
							</div>
							<div class="db-more"><i class="glyphicon glyphicon-arrow-down"></i> <span>Еще</span></div>
						</div>
					</div>							
				</div>
			</div>			
		</div>
	</div>
			
<!-- 	<div class="panel panel-default"> -->
<!-- 	  <div class="panel-heading">Загрузить новые публикации</div> -->
<!-- 	  <div class="panel-body"> <input type="file" id="file_input" webkitdirectory directory mozdirectory /> </div> -->
<!-- 	</div> -->
<!-- 	<div class="col-md-3"> -->
<!-- 		<div class="list-group"> -->
<!-- 			<a href="/files" class="list-group-item"> -->
<!-- 				To see -->
<!-- 			</a> -->
<!-- 			<a href="/files" class="list-group-item active"> -->
<!-- 				PIM -->
<!-- 			</a> -->
<!-- 			<a href="#" class="list-group-item"> -->
<!-- 				Knowledge Management -->
<!-- 			</a>					 -->
<!-- 			<a href="#" class="list-group-item"> -->
<!-- 				Категоризация -->
<!-- 			</a> -->
<!-- 			<a href="#" class="list-group-item"> -->
<!-- 				Обзоры -->
<!-- 			</a> -->
<!-- 		</div> -->
<!-- 	</div> -->
<!-- 	<div class="col-md-6"> -->
<!-- 		<div class="pim-selectable-list"> -->
<!-- 			<div class="pim-list-item">  -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Stuff I’ve Seen: A System for Personal Information Retrieval and Re-Use</span> -->
<!-- 				<span class="pim-year">2003</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item pim-selected"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">iMecho: An Associative Memory Based Desktop Search System</span> -->
<!-- 				<span class="pim-year">2009</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Desktop Gateway. Semantic Desktop Integration with Cloud Services</span> -->
<!-- 				<span class="pim-year">2013</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">OurSpaces – Design and Deployment of a Semantic Virtual Research Environment</span> -->
<!-- 				<span class="pim-year">2012</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">IRIS. Integrate. Relate. Infer. Share</span> -->
<!-- 				<span class="pim-year">2005</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Interlinking Personal Semantic Data on the Semantic Desktop and the Web of Data</span> -->
<!-- 				<span class="pim-year">2012</span> -->
<!-- 			</div>			 -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Introducing the Gnowsis Semantic Desktop</span> -->
<!-- 				<span class="pim-year">2004</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Feldspar. A System for Finding Information by Association</span> -->
<!-- 				<span class="pim-year">2008</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">iMemex. A Unified Approach to Personal Information Management</span> -->
<!-- 				<span class="pim-year">2006</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Haystack. A General-Purpose Information Management Tool for End Users Based on Semistructured Data</span> -->
<!-- 				<span class="pim-year">2003</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Haystack’s User Experience for Interacting with Semistructured Information</span> -->
<!-- 				<span class="pim-year">2003</span> -->
<!-- 			</div>			 -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">DeepaMehta — A Semantic Desktop</span> -->
<!-- 				<span class="pim-year">2005</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">SemEx. A Platform for Personal Information Management and Integration</span> -->
<!-- 				<span class="pim-year">2005</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Building a Semantic Representation for Personal Information (LiFiDeA)</span> -->
<!-- 				<span class="pim-year">2010</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">The NEPOMUK Project - On the way to the Social Semantic Desktop</span> -->
<!-- 				<span class="pim-year">2007</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Personal Knowledge Management with the Social Semantic Desktop</span> -->
<!-- 				<span class="pim-year"> </span> -->
<!-- 			</div>			 -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Why Personal Information Management (PIM) Technologies Are Not Widespread</span> -->
<!-- 				<span class="pim-year">2009</span> -->
<!-- 			</div> -->
<!-- 			<div class="pim-list-item"> -->
<!-- 				<span class="pim-draggable"> </span> -->
<!-- 				<span class="pim-title">Understanding the Evolution of Users’ Personal Information Management Practices</span> -->
<!-- 				<span class="pim-year">2007</span> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 	</div> -->
<!-- 	<div class="col-md-3"> -->

<!-- 		<div class="panel panel-default"> -->
<!-- 			<div class="panel-heading">Схожие статьи</div> -->
<!-- 			<div class="panel-body pim-related"> -->
<!-- 				<div><a href="#" class="scholar">Building a semantic representation for personal information</a></div> -->
<!-- 				<div><a href="#" class="scholar">Integrating memory context into personal information re-finding</a></div> -->
<!-- 				<div><a href="#" class="scholar">Information re-finding by context: a brain memory inspired approach</a></div> -->
<!-- 				<div><a href="#" class="scholar">Search your memory!-an associative memory based desktop search system</a></div> -->
<!-- 				<div class="more"><a href="#">Больше...</a></div> -->
<!-- 			</div> -->
<!-- 		</div>		 -->
		
<!-- 		<div class="panel panel-default"> -->
<!-- 			<div class="panel-heading">Свойства</div> -->
<!-- 			<div class="panel-body pim-details"> -->
<!-- 				<div>Наименование</div> -->
<!-- 				<div><textarea class="form-control">iMecho: An Associative Memory Based Desktop Search System</textarea></div> -->
<!-- 				<div>Авторы</div> -->
<!-- 				<div><a class="scholar" href="#">W Wu</a>, Jidong Chen, Hang Guo</div> -->
<!-- 				<div>Год</div> -->
<!-- 				<div>2009</div> -->
<!-- 				<div>Дата загрузки</div> -->
<!-- 				<div>03.02.2014 15:37</div>			 -->
<!-- 				<div>Размер</div> -->
<!-- 				<div>893 КБ</div> -->
<!-- 				<div class="more"><a href="#">Больше...</a></div> -->
<!-- 			</div> -->
<!-- 		</div>		 -->
<!-- 	</div> -->
</l:main-layout>