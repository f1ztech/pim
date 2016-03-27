package ru.mipt.pim.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;

@Service
public class PermissionService {

	@Autowired
	private UserService userService;

	public boolean canManage(Resource resource) {
		User user = userService.getCurrentUser();
		return user != null && resource != null && (resource.getOwner() == null || resource.getOwner().equals(user));
	}

}
