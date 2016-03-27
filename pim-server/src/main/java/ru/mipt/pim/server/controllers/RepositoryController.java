package ru.mipt.pim.server.controllers;

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.spring.RepositoryConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.services.ActivationService;

/**
 * RDF repositroy endpoint
 */
@Controller
@RequestMapping("/repository")
public class RepositoryController {

	@Resource
	private Repository repository;
	
	@Resource
	private EntityManager em;
	
	@Resource
	private RepositoryConnectionFactory connectionFactory;
	
	@Resource
	private ActivationService activationService;
	
	@Resource
	private ResourceRepository resourceRepository;	
	
	/**
	 * 
	 * @param query
	 * 	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>
	 *	 PREFIX pim:  <http://mipt.ru/pim/>
	 *	 describe ?result where {?result foaf:name ??name}"
	 *
	 * 
	 */
	@RequestMapping(value = "select")
	public void select(@RequestParam String query, Writer responseWriter) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
		RepositoryConnection connection = repository.getConnection();
		GraphQuery sparqlQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, query);
		try {
			org.openrdf.rio.RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, responseWriter);
			writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
			sparqlQuery.evaluate(writer);
		} finally {
			connection.close();
		}
	}
	
	/**
	 * 
	 *	@param json  {  
	 *					 "@id" : "urn:mipt.ru:pim:user:andreyb",  
	 *					 "@type" : "pim:User",  
	 *					 "pim:password" : "$2a$10$N4H4xWPCj355qJ4OGfgbr.CL14UmNU6YcTEd7LuBRvzbCWPvgHti6",  
	 *					 "foaf:nick" : "andreyb1asdf",  
	 *					 "@context" : {  
	 *					    "foaf" : "http://xmlns.com/foaf/0.1/",  
	 *					    "pim" : "http://mipt.ru/pim/"  
	 *					 }  
	 *				 } 
	 */
	@RequestMapping(value = "persist")
	public @ResponseBody String persist(@RequestParam String json, @RequestParam boolean replace, HttpServletResponse response) throws RepositoryException, RDFParseException, IOException, RDFHandlerException {
		RepositoryConnection connection = repository.getConnection();
		try {
			Model model = Rio.parse(IOUtils.toInputStream(json), null, RDFFormat.JSONLD);
			if (replace) {
				for (org.openrdf.model.Resource subject : model.stream().map(statement -> statement.getSubject()).collect(Collectors.toSet())) {
					removeStatements(connection, connection.getStatements(subject, null, null, true));
				}
			}
			for (Statement statement : model) {
				removeStatements(connection, connection.getStatements(statement.getSubject(), statement.getPredicate(), null, true));
				connection.add(statement);
			}
			connection.commit();
		} finally {
			connection.close();
		}
		return "{success : true}";
	}

	private void removeStatements(RepositoryConnection connection, RepositoryResult<Statement> statementsToRemove) throws RepositoryException {
		for (Statement statement : Iterations.asList(statementsToRemove)) {
			connection.remove(statement);
		}
	}

}
