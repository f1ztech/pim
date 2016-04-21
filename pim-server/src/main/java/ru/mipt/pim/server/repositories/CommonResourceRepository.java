package ru.mipt.pim.server.repositories;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.Query;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import ru.mipt.pim.server.index.Indexable;
import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.index.LanguageDetector;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.RepositoryService;
import ru.mipt.pim.server.services.UserHolder;
import ru.mipt.pim.util.Exceptions;
import ru.mipt.pim.util.RdfUtils;

public class CommonResourceRepository<T extends Resource> extends CommonRepository<T> {

	private static final String URI_PROPERTY = "uri";

	@Autowired
	private Repository repository;
	
	@Autowired
	private UserHolder userHolder;
	
	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private LanguageDetector languageDetector;
	
	@Autowired
	private RepositoryService repositoryService;

	private Class<T> clazz;

	public CommonResourceRepository(Class<T> clazz) {
		super(clazz);
		this.clazz = clazz;
	}

	@Override
	public void save(T object) {
		object.setDateCreated(new Date());
		object.setDateModified(new Date());
		super.save(object);
	}

	@Override
	public T merge(T object) {
//		object.setDateModified(new Date());
		T ret = super.merge(object);
		repositoryService.updateNarrowerResources(object);
		return ret;
	}

	@Override
	protected void beforeUpdate(T resource) {
		super.beforeUpdate(resource);
		
		if (resource.getOwner() == null && !resource.equals(userHolder.getCurrentUser())) {
			resource.setOwner(userHolder.getCurrentUser());
		}
	}
	
	@Override
	protected void afterUpdate(T resource) {
		if (resource instanceof Indexable) {
			indexingService.scheduleIndexing(resource, this::detectLanguage);
		}
	}

	protected String detectLanguage(T resource) {
		return languageDetector.detectLang(getLanguageString(resource));
	}

	protected String getLanguageString(T resource) {
		return StringUtils.defaultIfBlank(resource.getTitle(), resource.getName());
	}

	public List<T> findAll(User user) {
		Query query = prepareQuery("where { "
				+ "		?result rdf:type ??uri. "
				+ "		?result pim:owner ?user. "
				+ "		?user foaf:nick ??login "
				+ " }");
		query.setParameter("login", user.getLogin());
		try {
			query.setParameter(URI_PROPERTY, new java.net.URI(RdfUtils.getRdfUri(clazz)));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return getResultList(query);
	}
	
	public List<T> findAll() {
		try {
			Query query = prepareQuery("where { "
				+ "		?result rdf:type ??uri. "
				+ " }");
			query.setParameter(URI_PROPERTY, new java.net.URI(RdfUtils.getRdfUri(clazz)));
			return getResultList(query);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public List<T> findByTitle(User user, String title) {
		return makeTitleQuery(user, title).getResultList();
	}

	public T findFirstByTitle(User user, String title) {
		return getFirst(makeTitleQuery(user, title));
	}

	private Query makeTitleQuery(User user, String title) {
		Query query = prepareQuery(
				" where { " +
				"		 ?result nao:prefLabel ??title. " +
				" 		 ?result pim:owner ?user." +
				"		 ?user foaf:nick ??login " +
				" } ");
		query.setParameter("title", title);
		query.setParameter("login", user.getLogin());
		return query;
	}

	public List<T> findByTitleLike(User user, String title) throws URISyntaxException {
		Query query = prepareQuery(
				" where { " +
				"		 ?result nao:prefLabel ?title. " +
				" 		 ?result pim:owner ?user." +
				"		 ?user foaf:nick ??login. " +
				"		 ?result rdf:type ??classUri " +
				"		 FILTER regex(?title, ??title, 'i')" +
				" } ");
		query.setParameter("title", ".*" + title + ".*");
		query.setParameter("classUri", new java.net.URI(RdfUtils.getRdfUri(clazz))); // FIXME add this filters to all queries
		query.setParameter("login", user.getLogin());
		return getResultList(query);
	}

	public List<T> findByName(User user, String name) {
		return makeNameQuery(user, name).getResultList();
	}

	public T findFirstByName(User user, String name) {
		return getFirst(makeNameQuery(user, name));
	}

	private Query makeNameQuery(User user, String name) {
		Query query = prepareQuery(
				" where { " +
				"		 ?result dc:title ??name. " +
				" 		 ?result pim:owner ?user." +
				"		 ?user foaf:nick ??login " +
				" } ");
		query.setParameter("name", name);
		query.setParameter("login", user.getLogin());
		return query;
	}

	public List<T> find(String... properties) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return find(null, Exceptions.wrap(clazz::newInstance), properties);
	}
	
	public List<T> find(Consumer<QueryBuilder> queryConsumer, Supplier<T> resourceFactory, String... properties) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		RepositoryConnection connection = repository.getConnection();
		QueryBuilder qb = new QueryBuilder(connection);
		
		// prefixes
		RdfUtils.getNamespacesMap(clazz).forEach(qb::addPrefix);
		
		// select + where
		for (String property : properties) {
			if (property.equals(URI_PROPERTY)) {
				qb.select("?result");
			} else {
				String propertyBinding = "?" + property;
				qb.select(propertyBinding);
				qb.where("?result " + RdfUtils.getRdfProperty(clazz, property) + " " + propertyBinding);
			}
		}
		if (queryConsumer != null) {
			queryConsumer.accept(qb);
		}
		
		// result handling
		List<T> ret = new ArrayList<>();
		try {
			TupleQueryResult result = qb.buildTupleQuery().evaluate();
			try {
				while (result.hasNext()) {
					BindingSet binding = result.next();

					T resource = resourceFactory.get();
					ret.add(resource);
					
					for (String property : properties) {
						Value value = binding.getValue(property.equals(URI_PROPERTY) ? "result" : property);
						
						Assert.isInstanceOf(Literal.class, value);

						PropertyUtils.setProperty(resource, property, RdfUtils.literalToObject((Literal) value));
					}
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
