package ru.mipt.pim.adapters.fs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.stereotype.Component;

@Component
public class ConfigsService {

	private static final String PIM_SERVER_PROPERTY = "pim-server";
	private static final String WATCHED_DIRECTORIES_PROPERTY = "watched-directories";
	private static final String PROPERTIES_FILE = "fs-adapter.properties";

	private PropertiesConfiguration config;

	@PostConstruct
	public void init() throws ConfigurationException, IOException {
		File propertiesFile = new File(PROPERTIES_FILE);
		propertiesFile.createNewFile();
		config = new PropertiesConfiguration(propertiesFile);
		config.setAutoSave(true);
	}

	public Configuration getConfiguration() {
		return config;
	}

	public String getPimServerUrl() {
		return config.getString(PIM_SERVER_PROPERTY, "http://localhost:8080");
	}

	public List<String> getWatchedDirectories() {
		return (List<String>) (Object) config.getList(WATCHED_DIRECTORIES_PROPERTY);
	}

	public void setWatchedDirectories(List<String> directories) {
		config.setProperty(WATCHED_DIRECTORIES_PROPERTY, directories);
	}

	public void setPassword(String password) {
		config.setProperty("password", password);
	}

	public void setLogin(String login) {
		config.setProperty("login", login);
	}
	
	public String getPassword() {
		return config.getString("password");
	}
	
	public String getLogin() {
		return config.getString("login");
	}

	public boolean isDebug() {
		return config.getBoolean("debug", false);
	}
	
}
