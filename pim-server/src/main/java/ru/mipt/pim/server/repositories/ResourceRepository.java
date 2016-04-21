package ru.mipt.pim.server.repositories;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.index.IndexFinder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;

@Service
public class ResourceRepository extends CommonResourceRepository<Resource> {

	@Autowired
	private IndexFinder indexFinder;

	public ResourceRepository() {
		super(Resource.class);
	}

	public List<Resource> findRootResources(User user) {
		Query query = prepareQuery("where { ?result pim:owner ?user. "
								 + "		?user foaf:nick ??login "
								 + "		FILTER NOT EXISTS {?broaderResource skos:narrower ?result} }");
		query.setParameter("login", user.getLogin());
		return getResultList(query);
	}

	public List<Resource> findByFulltext(User user, String text) throws IOException, LangDetectException, ParseException {
		if (StringUtils.isBlank(text)) {
			return Collections.emptyList();
		}
		
		List<String> ids = indexFinder.findIdsByText(user, text);
		Query query = prepareQuery("where { "
				+ "		?result pim:id ?id. "
				+ "		FILTER(?id IN (" + ids.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",")) + ")) "
				+ "}");
		return getResultList(query);
	}
	
}
