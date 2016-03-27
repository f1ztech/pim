package ru.mipt.pim.server.services;

import info.aduna.iteration.Iterations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.ActivationRepository;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.util.Exceptions;

@Service
public class ActivationService {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static int activationDecay = 10;
	private static BigDecimal activationStep = new BigDecimal(50);
	private static BigDecimal maxActivation = new BigDecimal(100);
	private static int maxDepth = 3;
	
	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private ActivationRepository activationRepository;
	
	@Autowired
	private Repository repository;
	
	private List<URI> ignoredPredicates = new ArrayList<URI>();
	private ValueFactory valueFactory;
	private URI pActivationValue;
	
	private class ActivationSpreadingProcess {
		
		private List<Resource> activatedNodes = new ArrayList<>();
		private RepositoryConnection connection;
		
		public void start(RepositoryConnection connection, URI source) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
			this.connection = connection;
			
			spreadActivation(activationStep, source, findResourceProperties(source), 1);
		}
		
		private void spreadActivation(BigDecimal incomingActivation, Resource source, Set<Resource> resourcesToActivate, int depth) throws RepositoryException {
			activatedNodes.add(source);
			long start = System.currentTimeMillis();
			Statement activationValueTriple = Iterations.asList(connection.getStatements(source, pActivationValue, null, true)).stream().findFirst().orElse(null);
			logger.debug("spreadActivation:statements " + (System.currentTimeMillis() - start));
			BigDecimal activationValue = BigDecimal.ZERO; 
			if (activationValueTriple != null) {
				activationValue = ((Literal) activationValueTriple.getObject()).decimalValue();
				connection.remove(activationValueTriple);
				logger.debug("spreadActivation:remove " + (System.currentTimeMillis() - start));
			}
			
			float newActivation = activationValue.add(incomingActivation).min(maxActivation).floatValue();
			connection.add(source, pActivationValue, valueFactory.createLiteral(newActivation));
			logger.debug("spreadActivation:end " + (System.currentTimeMillis() - start));
			
			if (!resourcesToActivate.isEmpty() && depth < maxDepth) {
				BigDecimal outgoingActivation = incomingActivation.divide(new BigDecimal(resourcesToActivate.size()), 0, RoundingMode.HALF_UP);
				if (outgoingActivation.signum() > 0) {
					resourcesToActivate.stream().forEach(resource -> Exceptions.wrap(() -> {
						spreadActivation(outgoingActivation, resource, findResourceProperties(resource), depth + 1);
					}));
				}
			}
		}
		
		public Set<Resource> findResourceProperties(Resource resource) throws RepositoryException {
			long start = System.currentTimeMillis();
			Set<Resource> ret = new HashSet<Resource>();
			List<Statement> outgoingStatements = Iterations.asList(connection.getStatements(resource, null, null, true));
			outgoingStatements.stream().filter(st -> st.getObject() instanceof Resource && !ignoredPredicates.contains(st.getPredicate()) && !activatedNodes.contains(st.getObject()))
								  	   .forEach(st -> ret.add((Resource) st.getObject()));
			
			List<Statement> incomingStatements = Iterations.asList(connection.getStatements(null, null, resource, true));
			incomingStatements.stream().filter(st -> st.getSubject() instanceof Resource && !ignoredPredicates.contains(st.getPredicate()) && !activatedNodes.contains(st.getSubject()))
								  	   .forEach(st -> ret.add((Resource) st.getSubject()));
			logger.debug("findResourceProperties " + (System.currentTimeMillis() - start));
			return ret;			
		}	
	}
	
	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
		pActivationValue = valueFactory.createURI("http://mipt.ru/pim/activationValue");
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/owner"));
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/activationSubject"));
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/actionSubject"));
		ignoredPredicates.add(valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
	}
	
	public void spreadActivation(User user, ru.mipt.pim.server.model.Resource source) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
		RepositoryConnection connection = repository.getConnection();
		try {
			decayActivation(user, connection);
			
			ActivationSpreadingProcess spreadingProcess = new ActivationSpreadingProcess();
			spreadingProcess.start(connection, valueFactory.createURI(source.getUri()));
			
			connection.commit();
		} finally {
			connection.close();
		}
	}

	private void decayActivation(User user, RepositoryConnection connection) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
		activationRepository.decayActivation(user, connection, activationDecay);
	}
	
	public List<ru.mipt.pim.server.model.Resource> getTopActivatedResources(User user) {
		List<ru.mipt.pim.server.model.Resource> resources = activationRepository.findResourcesOrderedByActivation(user);
		return resources.size() > 5 ? resources.subList(0, 5) : resources;
		
	}
	
}
