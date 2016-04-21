package ru.mipt.pim.server.repositories;

import javax.persistence.Query;

import org.openrdf.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.User;

@Service
public class EmailFolderRepository extends CommonResourceRepository<EmailFolder> {

	@Autowired
	public Repository repository;

	public EmailFolderRepository() {
		super(EmailFolder.class);
	}

	public EmailFolder findByFolderId(User user, String folderId) {
		Query query = prepareQuery(
				" where { " +
				" 		 ?result pim:folderId ??id." +
				"		?result pim:owner ?user. " +
				"		?user foaf:nick ??login " +
				" } ");
		query.setParameter("id", folderId);
		query.setParameter("login", user.getLogin());
		return getFirst(query);
	}

}
