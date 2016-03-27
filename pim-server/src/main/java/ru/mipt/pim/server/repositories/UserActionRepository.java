package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.UserAction;


@Service
public class UserActionRepository extends CommonRepository<UserAction> {

	public UserActionRepository() {
		super(UserAction.class);
	}
	
}
