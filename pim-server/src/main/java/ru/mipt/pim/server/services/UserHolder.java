package ru.mipt.pim.server.services;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.UserRepository;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserHolder implements Serializable {

	private static final long serialVersionUID = -6014250614409703123L;
	private User currentUser;
	private Authentication authentication;

	@Autowired
	private UserRepository userRepository;

	public User getCurrentUser() {
		if (currentUser == null) {
			authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null) {
				currentUser = userRepository.findByLogin(authentication.getName());
			}
		}
		return currentUser;
	}
}
