package ru.mipt.pim.server.persistence;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.sesame.OpenRdfEmpireModule;

@Component
public class EntityManagerFactory implements FactoryBean<EntityManager> {

	private EntityManager entityManager;

	@Override
	public EntityManager getObject() throws Exception {
		if(entityManager == null) {
			System.setProperty("empire.configuration.file", new File(getClass().getClassLoader().getResource("META-INF/empire.configuration").toURI()).getAbsolutePath());
			Empire.init(new OpenRdfEmpireModule());
			entityManager = Persistence.createEntityManagerFactory("pim").createEntityManager();
		}
		return entityManager;
	}

	@Override
	public Class<?> getObjectType() {
		return EntityManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
