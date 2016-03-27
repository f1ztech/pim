package ru.mipt.pim.server.repositories;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.lucene.queryparser.classic.ParseException;
import org.openrdf.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.IndexingService;

import com.cybozu.labs.langdetect.LangDetectException;

@Service
public class ResourceRepository extends CommonResourceRepository<Resource> {

	@Autowired
	private Repository repository;

	@Autowired
	private IndexingService indexingService;

	public ResourceRepository() {
		super(Resource.class);
	}

	public List<Resource> findRootResources(User user) {
		Query query = prepareQuery("where { ?result <http://mipt.ru/pim/owner> ?user. "
								 + "		?user <http://xmlns.com/foaf/0.1/nick> ??login "
								 + "		FILTER NOT EXISTS {?broaderResource <http://www.w3.org/2004/02/skos/core#narrower> ?result} }");
		query.setParameter("login", user.getLogin());
		return query.getResultList();
	}

	public List<Resource> findByFulltext(User user, String text) throws IOException, LangDetectException, ParseException {
		List<String> ids = indexingService.findIdsByText(user, text);
		Query query = prepareQuery("where { "
				+ "		?result <http://mipt.ru/pim/id> ?id. "
				+ "		FILTER(?id IN (" + ids.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",")) + ")) "
				+ "}");
		return query.getResultList();
	}
	
	

}
