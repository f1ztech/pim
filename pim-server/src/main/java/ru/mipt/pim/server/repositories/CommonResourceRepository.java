package ru.mipt.pim.server.repositories;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mipt.pim.server.index.Indexable;
import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.UserHolder;
import ru.mipt.pim.util.RdfUtils;

public class CommonResourceRepository<T extends Resource> extends CommonRepository<T> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private Repository repository;
	
	@Autowired
	private UserHolder userHolder;
	
	@Autowired
	private IndexingService indexingService;

	private Class<T> clazz;
	private ValueFactory valueFactory;
	private URI pNarrower;

	public CommonResourceRepository(Class<T> clazz) {
		super(clazz);
		this.clazz = clazz;
	}

	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
		pNarrower = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#narrower");
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
		return super.merge(object);
	}

	@Override
	protected void afterResourceUpdate(T resource) {
		if (resource.getOwner() == null) {
			resource.setOwner(userHolder.getCurrentUser());
		}
		if (resource instanceof Indexable) {
			try {
				indexingService.indexResource(userHolder.getCurrentUser(), resource);
			} catch (Exception e) {
				logger.error("indexing error", e);
			}
		}
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
		return query.getResultList();
	}
	
	public List<T> findAll() {
		try {
			Query query = prepareQuery("where { "
				+ "		?result rdf:type ??uri. "
				+ " }");
			query.setParameter("uri", new java.net.URI(RdfUtils.getRdfUri(clazz)));
			return query.getResultList();
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
		return query.getResultList();
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

	public void addNarrowerResource(Resource parent, Resource child) throws RepositoryException {
		RepositoryConnection connection = repository.getConnection();
		try {
			URI parentUri = valueFactory.createURI(parent.getUri());
			URI childUri = valueFactory.createURI(child.getUri());
			connection.add(parentUri, pNarrower, childUri);
			connection.commit();
		} finally {
			connection.close();
		}
	}

	public void removeNarrowerResource(Resource parent, Resource child) throws RepositoryException {
		RepositoryConnection connection = repository.getConnection();
		try {
			URI parentUri = valueFactory.createURI(parent.getUri());
			URI childUri = valueFactory.createURI(child.getUri());
			connection.remove(parentUri, pNarrower, childUri);
			connection.commit();
		} finally {
			connection.close();
		}
	}
}
