package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;
import ru.mipt.pim.server.model.Contact;
import ru.mipt.pim.server.model.ContactGroup;
import ru.mipt.pim.server.model.User;

import javax.persistence.Query;

@Service
public class ContactGroupRepository extends CommonResourceRepository<ContactGroup> {

	public ContactGroupRepository() {
		super(ContactGroup.class);
	}
}
