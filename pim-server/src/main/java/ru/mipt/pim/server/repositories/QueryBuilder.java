package ru.mipt.pim.server.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.complexible.common.util.PrefixMapping;

public class QueryBuilder {

	private Map<String, String> prefixes = new HashMap<>();
	private List<String> selects;
	private List<String> conditions;
	private HashMap<String, Value> bindings;
	private RepositoryConnection connection;
	
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
		conditions.add(select);
		return this;
	}
	
	/**
	 * e.g. ?result rdf:type xxx
	 */
	public QueryBuilder where(String where) {
		conditions.add(where);
		return this;
	}

	/**
	 * e.g. "skos", "http://www.w3.org/2004/02/skos/core#"
	 */
	public QueryBuilder addPrefix(String prefix, String namespaceUri) {
		prefixes.put(prefix, namespaceUri);
		return this;
	}
	
	public QueryBuilder bind(String name, Value value) {
		bindings.put(name, value);
		return this;
	}
	
	public String buildQueryString() {
		StringBuilder builder = new StringBuilder();
		builder.append(prefixes.entrySet().stream()
				.map(p -> "PREFIX " + p.getKey() + ": <" + p.getValue() + ">")
				.collect(Collectors.joining("\n")));
		builder.append("\n");
		builder.append("select");
		builder.append(selects.stream().collect(Collectors.joining(",")));
		buildWhere(builder);
		
		return builder.toString();
	}

	private void buildWhere(StringBuilder builder) {
		builder.append(" where {");
		builder.append("\n");
		builder.append(conditions.stream().collect(Collectors.joining(".\n")));
		builder.append("\n");
		builder.append("}");
	}
	
	private void bindParameters(Query query) {
		bindings.forEach(query::setBinding);
	}
	
	public TupleQuery buildTupleQuery() throws RepositoryException, MalformedQueryException {
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, buildQueryString());
		bindParameters(query);
		return query;
	}
}
