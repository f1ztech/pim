package ru.mipt.pim.server.index;

import javax.annotation.PostConstruct;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.UserRepository;

@Component
public class IndexingJob {
//
//	private final Logger logger = LoggerFactory.getLogger(getClass());
//	
//	@Autowired
//	private Repository repository;
//	
//	@Autowired
//	private IndexingService indexingService;
//	
//	@Autowired
//	private UserRepository userRepository;
//
//	private ValueFactory valueFactory;
//	
//	@PostConstruct
//	public void init() {
//		valueFactory = repository.getValueFactory();
//	}
//	
//	@Scheduled(cron = "0 0 * * *") // every day
//	public void indexResources() {
//		for (User user : userRepository.findAll()) {
//			try {
//				indexResources(user);
//			} catch (Exception e) {
//				logger.error("user resources indexing error", e);
//			}
//		}
//	}
//
//	private void indexResources(User user) throws QueryEvaluationException, RepositoryException, MalformedQueryException {
//		RepositoryConnection connection = repository.getConnection();
//		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL,
//				"PREFIX pim: <http://mipt.ru/pim/> "
//				+ "PREFIX nao: <http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#> "
//				+ "PREFIX nie: <http://www.semanticdesktop.org/ontologies/2007/01/19/nie/> "
//				+ "select ?resourceId ?title ?content where { "
//				+ "	?resource pim:owner ?owner. "
//				+ "	?owner pim:id ??ownerId. "
//				+ " ?resource nao:prefLabel ?title. "
//				+ " ?resource pim:id ?resourceId. "
//				+ " optional { "
//				+ "		?resource nie:htmlContent ?content "
//				+ " }"
//				+ "}");
//		query.setBinding("ownerId", valueFactory.createLiteral(user.getId()));
//		try {
//			TupleQueryResult result = query.evaluate();
//			try {
//				while (result.hasNext()) {
//					BindingSet binding = result.next();
//					Literal resourceId = (Literal) binding.getValue("resourceId");
//					Literal title = (Literal) binding.getValue("title");
//					Literal content = (Literal) binding.getValue("content");
//					
//					indexResource(user, resourceId.stringValue(), title.stringValue(), content.stringValue());
//				}
//			} finally {
//				result.close();
//			}
//			
//		} finally {
//			connection.close();
//		}
//	}
//
//	private void indexResource(User user, String resourceId, String title, String content) {
//		try {
//			indexingService.indexResource(user, r -> {
//				r.setId(resourceId);
//				r.setTitle(title);
//				r.setContent(content);
//			});
//		} catch (Exception e) {
//			logger.error("resource indexing error", e);
//		}
//	}
	
}
