package ru.mipt.pim.server.repositories;

import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.openrdf.model.Literal;
import org.springframework.util.Assert;

import com.clarkparsia.empire.impl.RdfQuery;

import ru.mipt.pim.server.model.ObjectWithRdfId;
import ru.mipt.pim.util.RdfUtils;

public class CommonRepository<T extends ObjectWithRdfId> {

	private static WeakHashMap<String, Object> locks = new WeakHashMap<String, Object>();
	
	private Object getSyncObject(String id) {
//		return CommonRepository.class;
		synchronized (locks) {
			Object lock = locks.get(id);
			if (lock == null) {
				lock = new Object();
				locks.put(id, lock);
			}
			return lock;
		}
	}

	
	@javax.annotation.Resource
	protected EntityManager em;

	private Class<T> clazz;

	public CommonRepository(Class<T> clazz) {
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	protected <R> List<R> getResultList(Query query) {
		return query.getResultList();
	}
	
	protected T getFirst(Query query) {
		List<T> results = getResultList(query);
		return results.isEmpty() ? null : results.get(0);
	}

	protected Query prepareQuery(String sparql) {
		return prepareQuery(sparql, clazz);
	}
	
	protected Query prepareQuery(String sparql, Class<?> clazz) {
		Query aQuery = em.createQuery(sparql);
		aQuery.setHint(RdfQuery.HINT_ENTITY_CLASS, clazz);
		return aQuery;
	}

	public void save(T object) {
		beforeUpdate(object);
		synchronized (getSyncObject(object.getId())) {
			em.persist(object);
		}
		afterUpdate(object);
	}

	protected void beforeUpdate(T object) {
		if (object.getId() == null) {
			object.setId(UUID.randomUUID().toString());
		}
	}

	protected void afterUpdate(T object) {
	}

	public T merge(T object) {
		beforeUpdate(object);
		T result;
		synchronized (getSyncObject(object.getId())) {
			result = em.merge(object);
		}
		afterUpdate(object);
		return result;
	}

	public void remove(T object) {
		synchronized (getSyncObject(object.getId())) {
			em.remove(object);
		}
	}

	public T find(String uri) {
		return em.find(clazz, uri);
	}
	
	@SuppressWarnings("unchecked")
	public T findById(String id) {
		Query query = prepareQuery("where { ?result pim:id ??id. }");
		query.setParameter("id", id);
		return (T) query.getSingleResult();
	}
	
	public EntityManager getEm() {
		return em;
	}

}
