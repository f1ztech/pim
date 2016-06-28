package ru.mipt.pim.server.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.complexible.common.util.PrefixMapping;

import ru.mipt.pim.util.RdfUtils;

public class QueryBuilder {

	private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

	private static class Condition {
		private String condition;
		private boolean optional;
	
		public Condition(String condition, boolean optional) {
			this.condition = condition;
			this.optional = optional;
		}
		
		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
		public boolean isOptional() {
			return optional;
		}
		public void setOptional(boolean optional) {
			this.optional = optional;
		}
		
		public String toQuery() {
			return optional ? "optional { " + condition + "} " : condition.replaceAll("\\.?\\s*$", "\\.");
		}
	}
	
	private Map<String, String> prefixes = new HashMap<>();
	private List<String> selects = new ArrayList<>();
	private List<Condition> conditions = new ArrayList<>();
	private List<String> filters = new ArrayList<>();
	private List<String> orderExpressions = new ArrayList<>();
	private HashMap<String, Value> bindings = new HashMap<>();
	private RepositoryConnection connection;
	
	public QueryBuilder() {
		this(null);
	}
	
	public QueryBuilder(RepositoryConnection connection) {
		this.connection = connection;
		addDefaultPrefixes();
	}
	
	private void addDefaultPrefixes() {
		PrefixMapping.GLOBAL.getPrefixes().forEach(prefix -> {
			addPrefix(prefix, PrefixMapping.GLOBAL.getNamespace(prefix));
		});
	}

	/**
	 * e.g. ?result
	 */
	public QueryBuilder select(String select) {
		selects.add(select);
		return this;
	}
	
	/**
	 * e.g. ?result rdf:type xxx
	 */
	public QueryBuilder where(String condition) {
		return where(condition, false);
	}
	
	/**
	 * e.g. ?result rdf:type xxx
	 */
	public QueryBuilder where(String condition, boolean optional) {
		conditions.add(new Condition(condition, optional));
		return this;
	}
	
	/**
	 * e.g. NOT EXISTS {?broaderResource skos:narrower ?result}
	 */
	public QueryBuilder filter(String filter) {
		filters.add(filter);
		return this;
	}
	
	/**
	 * e.g. DESC(?name)
	 */
	public QueryBuilder orderBy(String orderBy) {
		orderExpressions.add(orderBy);
		return this;
	}


	/**
	 * e.g. "skos", "http://www.w3.org/2004/02/skos/core#"
	 */
	public QueryBuilder addPrefix(String prefix, String namespaceUri) {
		prefixes.put(prefix, namespaceUri);
		return this;
	}
	
	public QueryBuilder bind(String name, Object object) {
		Value value = object instanceof Value ? (Value) object : RdfUtils.objectToValue(object);
		bindings.put(name, value);
		return this;
	}
	
	public String buildQueryString() {
		StringBuilder builder = new StringBuilder();
		builder.append(prefixes.entrySet().stream()
				.map(p -> "PREFIX " + p.getKey() + ": <" + p.getValue() + ">")
				.collect(Collectors.joining("\n")));
		builder.append("\n");
		builder.append("SELECT ");
		builder.append(selects.stream().collect(Collectors.joining(" ")));
		builder.append("\n");
		builder.append(buildWhere());
		
		return builder.toString();
	}

	public String buildWhere() {
		StringBuilder builder = new StringBuilder();
		builder.append("where {");
		builder.append("\n");
		builder.append(conditions.stream().map(Condition::toQuery).collect(Collectors.joining("\n")));
		builder.append("\n");
		builder.append(filters.stream()
				.map(filter -> " FILTER " + filter)
				.collect(Collectors.joining("\n")));
		builder.append("\n");
		builder.append("}");
		builder.append("\n");
		if (!orderExpressions.isEmpty()) {
			builder.append("ORDER BY ");
			builder.append(orderExpressions.stream().collect(Collectors.joining(" ")));
		}
		return builder.toString();
	}
	
	public HashMap<String, Value> getBindings() {
		return bindings;
	}
	
	private void bindParameters(Query query) {
		bindings.forEach(query::setBinding);
	}
	
	public TupleQuery buildTupleQuery() throws RepositoryException, MalformedQueryException {
		return buildTupleQuery(connection);
	}
	
	public TupleQuery buildTupleQuery(RepositoryConnection connection) throws RepositoryException, MalformedQueryException {
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, buildQueryString());
		bindParameters(query);
		return query;
	}
	
	public javax.persistence.Query buildJpaQuery(EntityManager em) {
		javax.persistence.Query query = em.createQuery(buildWhere());
		bindings.forEach((name, value) -> {
			Assert.isInstanceOf(Literal.class, value, "Only literals can be bound to javax.persistence.Query");
			query.setParameter(name, RdfUtils.literalToObject((Literal) value));	
		});
		return query;
	}
}
