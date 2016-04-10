package ru.mipt.pim.server.mail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

import com.google.api.client.util.store.FileDataStoreFactory;

@Service
public class MailAdapterService {

	@Autowired
	private FileStorageService fileStorageService;

	private HashMap<User, MailAdapter> userAdapters = new HashMap<>();
	private FileDataStoreFactory dataStoreFactory;

	@PostConstruct
	private void init() throws IOException {
		File dataDirectory = new File(fileStorageService.getAbsolutePath("gmail-data-store"));
		dataDirectory.mkdir();
		dataStoreFactory = new FileDataStoreFactory(dataDirectory);
	}

	public MailAdapter getAdapter(User user) throws IOException {
		MailAdapter adapter = userAdapters.get(user.getId());
		if (adapter == null && user.getUserConfigs() != null && user.getUserConfigs().getOauthEmailUser() != null) {
			adapter = new GmailAdapter(user, dataStoreFactory, this);
			userAdapters.put(user, adapter);
		}
		return adapter;
	}

}
