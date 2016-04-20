package ru.mipt.pim.server.services;

import java.util.Comparator;

import javax.annotation.PostConstruct;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Resource;

@Service
public class ResourceComparator implements Comparator<Resource> {

	@Autowired
	private Repository repository;

	private ValueFactory valueFactory;
	private URI pNarrower;

	@Override
	public int compare(Resource o1, Resource o2) {
		if (isHasNarrowerResources(o1) && !isHasNarrowerResources(o2)) {
			return -1;
		}
		if (isHasNarrowerResources(o2) && !isHasNarrowerResources(o1)) {
			return 1;
		}
		return o1.compareTo(o2);
	}


	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
		pNarrower = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#narrower");
	}

	public boolean isHasNarrowerResources(Resource resource) {
		if (resource.getHasNarrowerResources() == null) {
			try {
				RepositoryConnection connection = repository.getConnection();
				try {
					resource.setHasNarrowerResources(connection.hasStatement(valueFactory.createURI(resource.getUri()), pNarrower, null, true));
				} finally {
					connection.close();
				}

			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}
		return resource.getHasNarrowerResources();
	}


}
