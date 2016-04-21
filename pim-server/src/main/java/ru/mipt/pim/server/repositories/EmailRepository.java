package ru.mipt.pim.server.repositories;


import java.util.HashSet;
import java.util.Set;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Email;
import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.User;

@Service
public class EmailRepository extends CommonResourceRepository<Email> {

	@Autowired
	public Repository repository;
	private ValueFactory valueFactory;

	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
	}
	
	public EmailRepository() {
		super(Email.class);
	}

	public Set<String> getMessageIds(User user, EmailFolder folder) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Set<String> ret = new HashSet<>();
		
		RepositoryConnection connection = repository.getConnection();
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL,
				  "PREFIX pim: <http://mipt.ru/pim/> "
				+ "PREFIX nmo: <http://www.semanticdesktop.org/ontologies/2007/03/22/nmo/#> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select ?messageId where { "
				+ "	?mail pim:owner ?owner. "
				+ "	?owner pim:id ??ownerId. "
				+ " ?folder skos:narrower ?mail."
				+ " ?folder pim:id ??folderId. "
				+ " ?mail nmo:messageId ?messageId. "
				+ "}");
		query.setBinding("ownerId", valueFactory.createLiteral(user.getId()));
		query.setBinding("folderId", valueFactory.createLiteral(folder.getId()));
		try {
			TupleQueryResult result = query.evaluate();
			try {
				while (result.hasNext()) {
					BindingSet binding = result.next();
					ret.add(((Literal) binding.getValue("messageId")).stringValue());
				}
			} finally {
				result.close();
			}
			
		} finally {
			connection.close();
		}
		
		return ret;
	}	
}
