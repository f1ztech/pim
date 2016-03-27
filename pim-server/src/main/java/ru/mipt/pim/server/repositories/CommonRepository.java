package ru.mipt.pim.server.repositories;

import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ru.mipt.pim.server.model.ObjectWithRdfId;

import com.clarkparsia.empire.impl.RdfQuery;

@SuppressWarnings({ "unchecked" })
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

	protected T getFirst(Query query) {
		List<T> results = query.getResultList();
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
		populateId(object);
		synchronized (getSyncObject(object.getId())) {
			em.persist(object);
		}
	}

	protected void populateId(T object) {
		if (object.getId() == null) {
			object.setId(UUID.randomUUID().toString());
		}
	}

	public T merge(T object) {
		populateId(object);
		synchronized (getSyncObject(object.getId())) {
			return em.merge(object);
		}
	}

	public void remove(T object) {
		synchronized (getSyncObject(object.getId())) {
			em.remove(object);
		}
	}

	public T find(String uri) {
		return em.find(clazz, uri);
	}
	
	public T findById(String id) {
		Query query = prepareQuery("where { ?result <http://mipt.ru/pim/id> ??id. }");
		query.setParameter("id", id);
		return (T) query.getSingleResult();
	}
	
}
