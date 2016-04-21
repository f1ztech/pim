package ru.mipt.pim.server.repositories;

import java.net.URI;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.User;

@Service
public class FolderRepository extends CommonResourceRepository<Folder> {

	public FolderRepository() {
		super(Folder.class);
	}
	
	public List<Folder> findRootFolders(User user) {
		Query query = prepareQuery("where { ?result pim:owner ?user. "
								 + "		?user foaf:nick ??login "
								 + "		FILTER NOT EXISTS {?result <http://mipt.ru/pim/narrowerResource> ?subFolder} }");
		query.setParameter("login", user.getLogin());
		return getResultList(query);
	}

	public Folder findByPath(User user, String path) {
		Query query = prepareQuery("where { "
								 + "		?result <http://mipt.ru/pim/path> ??path. "				
								 + " 		?result pim:owner ?user."
								 + "		?user foaf:nick ??login }");
		query.setParameter("path", path);
		query.setParameter("login", user.getLogin());
		return getFirst(query);
	}
	
	public Folder findById(String id) {
		return em.find(Folder.class, URI.create("http://mipt.ru/pim/folder#" + id));
	}

}
