package ru.mipt.pim.server.services;

import java.util.Comparator;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
		if (o1.getTitle() != null && o2.getTitle() != null) {
			return o1.getTitle().compareTo(o2.getTitle());
		}
		if (o1.getName() != null && o2.getName() != null) {
			return o1.getName().compareTo(o2.getName());
		}
		return o1.getId() == null ? -1 : o2.getId() == null ? 1 : o1.getId().compareTo(o2.getId());
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
