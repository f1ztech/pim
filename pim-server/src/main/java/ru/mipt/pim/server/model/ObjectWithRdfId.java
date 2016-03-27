package ru.mipt.pim.server.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.beanutils.PropertyUtils;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ru.mipt.pim.util.RdfUtils;

@JsonIgnoreProperties(value = { "handler", "allTriples", "instanceTriples", "interfaceClass" })
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos",  "http://www.w3.org/2004/02/skos/core#",
	"nao", "http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#"
})
public abstract class ObjectWithRdfId implements SupportsRdfId {

	public static class InvertedList<T> extends ArrayList<T> {

		private String mappedBy;
		private Object source;
		private static final long serialVersionUID = -4154006493441991012L;
		private EntityManager em;

		public InvertedList(List<T> initial, Object source, String mappedBy) {
			super(initial);
			this.source = source;
			this.mappedBy = mappedBy;
		}

		@Override
		public boolean add(T el) {
			boolean ret = getMappedList(el).add(source);
			getEm().merge(el);
			return ret;
		}

		private List<Object> getMappedList(Object el) {
			try {
				List<Object> mappedList = (List<Object>) PropertyUtils.getProperty(el, mappedBy);
				return mappedList;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private EntityManager getEm() {
			if (em == null) {
				em = Persistence.createEntityManagerFactory("pim").createEntityManager();
			}
			return em;
		}

		@Override
		public void add(int index, T element) {
			add(element);
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			c.forEach(el -> add(el));
			return true;
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			return addAll(c);
		}

		@Override
		public T remove(int index) {
			T o = get(index);
			remove(o);
			return o;
		}

		@Override
		public boolean remove(Object o) {
			boolean ret = getMappedList(o).remove(source);
			getEm().merge(o);
			return ret;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			c.forEach(this::remove);
			return true;
		}

		@Override
		public void clear() {
			this.forEach(this::remove);
		}
	}

	private HashMap<String, List<? extends ObjectWithRdfId>> invertedProperties = new HashMap<>();

	/**
	 * Default support for the ID of an RDF concept
	 */
	private SupportsRdfId mIdSupport = new SupportsRdfIdImpl();

	@RdfProperty("pim:id")
	private String id;
	/**
	 * @inheritDoc
	 */
	@Override
	@JsonIgnore
	public RdfKey<?> getRdfId() {
		return mIdSupport.getRdfId();
	}

	@JsonIgnore
	public String getRdfIdNamespace() {
		return RdfUtils.getRdfUri(getRealClass()) + ":";
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setRdfId(final RdfKey theId) {
		mIdSupport.setRdfId(theId);
	}

	public void setId(String id) {
		try {
			if (id != null) {
				setRdfId(new SupportsRdfId.URIKey(new URI(getRdfIdNamespace() + id)));
				this.id = id;
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("Bad uri", e);
		}
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(final Object theObj) {
		if (this == theObj) {
			return true;
		}

		if (theObj == null || !(theObj instanceof SupportsRdfId)) {
			return false;
		}

		SupportsRdfId objWithId = (SupportsRdfId) theObj;

		if (getRdfId() != null) {
			return getRdfId().equals(objWithId.getRdfId());
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId() == null ? 0 : getRdfId().value().hashCode();
	}

	public String getUri() {
		return getRdfId() == null ? null : getRdfId().toString();
	}

	protected <T extends ObjectWithRdfId> List<T> getInvertedList(Class<T> objectClazz, Class<?> listClazz, String mappedBy) {
		List<T> results = (List<T>) invertedProperties.get(mappedBy);
		if (results == null) {
			RdfProperty rdfPropertyAnnotation;
			try {
				rdfPropertyAnnotation = objectClazz.getDeclaredField(mappedBy).getAnnotation(RdfProperty.class);
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}

			String property = rdfPropertyAnnotation.value();
			property = RdfUtils.expandNamespace(objectClazz, property);

			EntityManager em = Persistence.createEntityManagerFactory("pim").createEntityManager();
			Query aQuery = em.createQuery(" where { " +
					"		 ?result <" + property + "> ?this." +
					"		 FILTER (?this = <" + getRdfId().toString() + ">) " +
					" } ");
			aQuery.setHint(com.clarkparsia.empire.impl.RdfQuery.HINT_ENTITY_CLASS, Resource.class);
			results = new InvertedList<T>(aQuery.getResultList(), this, mappedBy);

			invertedProperties.put(mappedBy, results);
		}
		return results;
	}

	public Class<? extends ObjectWithRdfId> getRealClass() {
		// workaround for empire proxies
		Class<? extends ObjectWithRdfId> thisClass = getClass();
		return thisClass.getName().endsWith("Impl") ? (Class<? extends ObjectWithRdfId>) thisClass.getSuperclass() : thisClass;
	}

}
