package ru.mipt.pim.server.services;

import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.util.RdfUtils;

@Component
public class RepositoryService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Repository repository;
	
	private ValueFactory valueFactory;
	private URI pNarrower;
	private URI pHasNarrower;
	
	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
		pNarrower = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#narrower");
		pHasNarrower = valueFactory.createURI("http://mipt.ru/pim/hasNarrowerResources");
	}

	public void addNarrowerResource(Resource parent, Resource child) throws RepositoryException {
		RepositoryConnection connection = repository.getConnection();
		try {
			URI parentUri = valueFactory.createURI(parent.getUri());
			URI childUri = valueFactory.createURI(child.getUri());
			connection.add(parentUri, pNarrower, childUri);
			connection.remove(parentUri, pHasNarrower, null);
			connection.add(parentUri, pHasNarrower, valueFactory.createLiteral(true));
			connection.commit();
		} finally {
			connection.close();
		}
	}
	
	public void updateNarrowerResources(Resource resource) {
		try {
			URI resourceUri = valueFactory.createURI(resource.getUri());
			RepositoryConnection connection = repository.getConnection();
			RepositoryResult<Statement> statements = connection.getStatements(resourceUri, pNarrower, null, true);
			connection.remove(resourceUri, pHasNarrower, null);
			connection.add(resourceUri, pHasNarrower, valueFactory.createLiteral(statements.hasNext()));
			connection.commit();
		} catch (RepositoryException e) {
			logger.error("setHasNarrowerResources error", e);
		}
	}

	public void removeNarrowerResource(Resource parent, Resource child) throws RepositoryException {
		RepositoryConnection connection = repository.getConnection();
		try {
			URI parentUri = valueFactory.createURI(parent.getUri());
			URI childUri = valueFactory.createURI(child.getUri());
			connection.remove(parentUri, pNarrower, childUri);
			updateNarrowerResources(parent);
			connection.commit();
		} finally {
			connection.close();
		}
	}
	
	public void setProperty(Resource resource, String propertyUri, Function<ValueFactory, Value> valueProvider) {
		try {
			RepositoryConnection connection = repository.getConnection();
			try {
				setProperty(connection, resource, propertyUri, valueProvider);
				connection.commit();
			} finally {
				connection.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setProperty(RepositoryConnection connection, Resource resource, String propertyUri, Function<ValueFactory, Value> valueProvider) {
		try {
			URI parentUri = valueFactory.createURI(resource.getUri());
			URI property = valueFactory.createURI(RdfUtils.expandNamespace(resource.getClass(), propertyUri));
			connection.remove(parentUri, property, null);
			connection.add(parentUri, property, valueProvider.apply(valueFactory));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
