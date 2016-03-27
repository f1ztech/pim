package ru.mipt.pim.server.web;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.UserService;

@Component
public class WebFunctions {

	private static WebFunctions instance;

	@Autowired
	private UserService userService;

	@PostConstruct
	public void init() {
		instance = this;
	}

	public static WebFunctions getInstance() {
		return instance;
	}

	public String concat(String... strings) {
		return String.join("", strings);
	}

	public boolean isLoggedIn() {
		return userService.isLoggedIn();
	}

	public User getCurrentUser() {
		return userService.getCurrentUser();
	}

}
