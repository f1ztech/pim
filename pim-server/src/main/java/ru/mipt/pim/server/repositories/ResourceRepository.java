package ru.mipt.pim.server.repositories;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clarkparsia.empire.annotation.RdfProperty;
import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.index.IndexFinder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.presentations.BeanWrapper;
import ru.mipt.pim.util.RdfUtils;
import ru.mipt.pim.util.Utilities;

@Service
public class ResourceRepository extends CommonResourceRepository<Resource> {

	@Autowired
	private IndexFinder indexFinder;

	public ResourceRepository() {
		super(Resource.class);
	}

	public List<Resource> findRootResources(User user) {
		return getResultList(rootResourceQueryBuilder(user).buildJpaQuery(em));
	}
	
	public List<BeanWrapper<Resource>> findRootResources(User user, Consumer<BeanWrapper<Resource>> resourceAdjuster, String... properties) throws Exception {
		return find(rootResourceQueryBuilder(user), BeanWrapper::new, resourceAdjuster, properties);
	}
	
	private QueryBuilder rootResourceQueryBuilder(User user) {
		QueryBuilder qb = new QueryBuilder().where("?result pim:owner ?user")
			.where("?user foaf:nick ??login")
			.bind("login", user.getLogin())
			.filter("NOT EXISTS { ?broaderResource skos:narrower ?result }");
			
		addResourceOrder(qb);
		return qb;
	}

	private void addResourceOrder(QueryBuilder qb) {
		qb.where("?result pim:hasNarrowerResources ?hasNarrower", true)
			.where("?result nao:prefLabel ?title", true)
			.where("?result dc:title ?name", true)
			.orderBy("DESC(?hasNarrower)")
			.orderBy("?title")
			.orderBy("?name");
	}
	
	public List<BeanWrapper<Resource>> findNarrower(String resourceId, Consumer<BeanWrapper<Resource>> resourceAdjuster, String... properties) throws Exception {
		QueryBuilder qb = new QueryBuilder().where("?resource skos:narrower ?result")
				.where("?resource pim:id ??id")
				.bind("id", resourceId);
		addResourceOrder(qb);
		
		return find(qb, BeanWrapper::new, resourceAdjuster, properties);
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
