package ru.mipt.pim.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.mipt.pim.server.mail.MailAdapter;
import ru.mipt.pim.server.mail.MailAdapterService;
import ru.mipt.pim.server.mail.MailException;
import ru.mipt.pim.server.mail.MessageFolder;
import ru.mipt.pim.server.mail.OAuthAdapter;
import ru.mipt.pim.server.mail.RequireOauthAuthenticationException;
import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.model.UserConfigs;
import ru.mipt.pim.server.repositories.EmailFolderRepository;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.repositories.UserConfigsRepository;
import ru.mipt.pim.server.repositories.UserRepository;
import ru.mipt.pim.server.services.UserService;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@Scope()
@RequestMapping("/mail")
public class MailController {

	@Autowired
	public ResourceRepository resourceRepository;

	@Autowired
	public EmailFolderRepository emailFolderRepository;

	@Autowired
	public UserService userService;

	@Autowired
	public UserConfigsRepository userConfigsRepository;

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public MailAdapterService mailAdapterService;

	public static class UserConfigsResponse {
		private UserConfigs userConfigs;
		private List<EmailFolder> allFolders;

		public List<EmailFolder> getAllFolders() {
			return allFolders;
		}

		public void setAllFolders(List<EmailFolder> allFolders) {
			this.allFolders = allFolders;
		}

		public UserConfigs getUserConfigs() {
			return userConfigs;
		}

		public void setUserConfigs(UserConfigs userConfigs) {
			this.userConfigs = userConfigs;
		}
	}

	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(RequireOauthAuthenticationException.class)
	private @ResponseBody ObjectNode oauthAuthenticationError() throws MailException, IOException {
		return makeOauthAuthenticationUrlResponse(mailAdapterService.getAdapter(userService.loadCurrentUser()));
	}

	private OAuthAdapter getOauthAdapter() throws IOException {
		return (OAuthAdapter) getMailAdapter();
	}

	private MailAdapter getMailAdapter() throws IOException {
		return mailAdapterService.getAdapter(userService.loadCurrentUser());
	}

	@RequestMapping(value = "userConfigs", method = RequestMethod.POST)
	public @ResponseBody ObjectNode saveUserConfigs(@RequestBody UserConfigs body) throws IOException, MailException {
		User currentUser = userService.loadCurrentUser();

		UserConfigs configs = currentUser.getUserConfigs();
		if (configs == null) {
			configs = new UserConfigs();
			currentUser.setUserConfigs(configs);
		}
		configs.setOauthEmailUser(body.getOauthEmailUser());
		configs.getSynchronizedEmailFolders().forEach(f -> emailFolderRepository.remove(f));
		configs.setSynchronizedEmailFolders(body.getSynchronizedEmailFolders());

		configs.getSynchronizedEmailFolders().forEach(f -> emailFolderRepository.save(f));
		userConfigsRepository.merge(configs);
		userRepository.merge(currentUser);

		MailAdapter mailAdapter = mailAdapterService.getAdapter(currentUser);
		if (mailAdapter instanceof OAuthAdapter && !((OAuthAdapter) mailAdapter).isHasCredential()) {
			return makeOauthAuthenticationUrlResponse(mailAdapter);
		} else {
			return null;
		}
	}

	private ObjectNode makeOauthAuthenticationUrlResponse(MailAdapter mailAdapter) throws MailException {
		return JsonNodeFactory.instance.objectNode().put("authorizationUrl", ((OAuthAdapter) mailAdapter).getAuthenticateUrl());
	}

	@RequestMapping(value = "userConfigs", method = RequestMethod.GET)
	public @ResponseBody UserConfigsResponse getUserConfigs() throws IOException, MailException {
		UserConfigsResponse ret = new UserConfigsResponse();
		User currentUser = userService.loadCurrentUser();
		ret.setUserConfigs(currentUser.getUserConfigs());
		MailAdapter mailAdapter = getMailAdapter();
		if (mailAdapter != null) {
			ret.setAllFolders(mailAdapter.getFolders().stream()
					.map(f -> new EmailFolder(f.getName(), f.getId()))
					.sorted().collect(Collectors.toList()));
		}
		return ret;
	}

	@RequestMapping("oauthCallback")
	public String oauthCallback(@RequestParam("code") String authCode) throws IOException, MailException {
		getOauthAdapter().makeCredential(authCode);
		return "redirect:/#/mail/oauthCallback";
	}

}
