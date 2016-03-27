package ru.mipt.pim.server.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;


@Service
public class ActivationRepository extends CommonRepository<Resource> {

	@javax.annotation.Resource
	private Repository repository;
	
	public ActivationRepository() {
		super(Resource.class);
	}
	
	public void decayActivation(User user, RepositoryConnection connection, int activationDecay) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
		System.out.println(new Date());
		Update decayActivationQuery = connection.prepareUpdate(QueryLanguage.SPARQL, 
				"PREFIX pim: <http://mipt.ru/pim/> " +
				"DELETE {?s pim:activationValue ?oldActivation} " +
				"INSERT { " +
				"	?s pim:activationValue ?newActivation " +
				"} " +
				"WHERE  { " + 
				"	?s pim:activationValue ?oldActivation. " +
				"	?s pim:owner ?u. " +
				"	?u pim:id \"" + user.getId() + "\". " +
				"	BIND (IF (?oldActivation < " + activationDecay + ", \"0\"^^xsd:float, ?oldActivation - " + activationDecay + ") as ?newActivation) " +				
				"}");
//		decayActivationQuery.setBinding("decay", repository.getValueFactory().createLiteral(activationDecay));
//		decayActivationQuery.setBinding("user", repository.getValueFactory().createLiteral(user.getId()));
		decayActivationQuery.execute();
		System.out.println(new Date());
	}

	public List<Resource> findResourcesOrderedByActivation(User user) {
		Query query = prepareQuery("where { "
				+ "		?result <http://mipt.ru/pim/owner> ?user. "
				+ "		?user <http://xmlns.com/foaf/0.1/nick> ??login. "
				+ "		?result <http://mipt.ru/pim/activationValue> ?activation. "
				+ "		filter (?activation > 5) "
				+ "}"
				+ " order by desc(?activation)", Resource.class);
		query.setParameter("login", user.getLogin());
		return query.getResultList();
	}
	
}
