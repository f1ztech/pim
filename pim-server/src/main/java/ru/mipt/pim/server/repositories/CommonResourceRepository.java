package ru.mipt.pim.server.repositories;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mipt.pim.server.index.Indexable;
import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.index.LanguageDetector;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.RepositoryService;
import ru.mipt.pim.server.services.UserHolder;
import ru.mipt.pim.util.RdfUtils;

public class CommonResourceRepository<T extends Resource> extends CommonRepository<T> {

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
				+ "		?result <http://mipt.ru/pim/owner> ?user. "
				+ "		?user <http://xmlns.com/foaf/0.1/nick> ??login "
				+ " }");
		query.setParameter("login", user.getLogin());
		try {
			query.setParameter("uri", new java.net.URI(RdfUtils.getRdfUri(clazz)));
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
			query.setParameter("uri", new java.net.URI(RdfUtils.getRdfUri(clazz)));
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
				"		 ?result <http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#prefLabel> ??title. " +
				" 		 ?result <http://mipt.ru/pim/owner> ?user." +
				"		 ?user <http://xmlns.com/foaf/0.1/nick> ??login " +
				" } ");
		query.setParameter("title", title);
		query.setParameter("login", user.getLogin());
		return query;
	}

	public List<T> findByTitleLike(User user, String title) throws URISyntaxException {
		Query query = prepareQuery(
				" where { " +
				"		 ?result <http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#prefLabel> ?title. " +
				" 		 ?result <http://mipt.ru/pim/owner> ?user." +
				"		 ?user <http://xmlns.com/foaf/0.1/nick> ??login. " +
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
				"		 ?result <http://purl.org/dc/terms/title> ??name. " +
				" 		 ?result <http://mipt.ru/pim/owner> ?user." +
				"		 ?user <http://xmlns.com/foaf/0.1/nick> ??login " +
				" } ");
		query.setParameter("name", name);
		query.setParameter("login", user.getLogin());
		return query;
	}

}
