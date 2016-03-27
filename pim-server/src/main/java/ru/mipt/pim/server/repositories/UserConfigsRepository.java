package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.UserConfigs;


@Service
public class UserConfigsRepository extends CommonRepository<UserConfigs> {

	public UserConfigsRepository() {
		super(UserConfigs.class);
	}

}
