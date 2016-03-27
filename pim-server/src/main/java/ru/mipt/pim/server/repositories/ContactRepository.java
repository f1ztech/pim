package ru.mipt.pim.server.repositories;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Contact;
import ru.mipt.pim.server.model.User;

@Service
public class ContactRepository extends CommonResourceRepository<Contact> {

	public ContactRepository() {
		super(Contact.class);
	}

	public Contact findByEmail(User user, String email) {
		Query query = prepareQuery("where { "
				 + "		?result <http://www.semanticdesktop.org/ontologies/2007/03/22/nco/#hasEmailAddress> ??email. "
				 + " 		?result <http://mipt.ru/pim/owner> ?user."
				 + "		?user <http://xmlns.com/foaf/0.1/nick> ??login }");
		query.setParameter("email", email);
		query.setParameter("login", user.getLogin());
		return getFirst(query);
	}
}
