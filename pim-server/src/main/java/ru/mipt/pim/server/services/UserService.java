package ru.mipt.pim.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserHolder userHolder;

	@Autowired
	private UserRepository userRepository;

	public User getCurrentUser() {
		return userHolder.getCurrentUser();
	}

	public User loadCurrentUser() {
		return userRepository.findById(userHolder.getCurrentUser().getId());
	}

	public boolean isLoggedIn() {
		return getCurrentUser() != null;
	}

}
