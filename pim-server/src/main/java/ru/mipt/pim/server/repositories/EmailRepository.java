package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Email;

@Service
public class EmailRepository extends CommonRepository<Email> {

	public EmailRepository() {
		super(Email.class);
	}

}
