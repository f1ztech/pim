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
				" 		 ?result <http://mipt.ru/pim/folderId> ??id." +
				"		?result <http://mipt.ru/pim/owner> ?user. " +
				"		?user <http://xmlns.com/foaf/0.1/nick> ??login " +
				" } ");
		query.setParameter("id", folderId);
		query.setParameter("login", user.getLogin());
		return getFirst(query);
	}

}
