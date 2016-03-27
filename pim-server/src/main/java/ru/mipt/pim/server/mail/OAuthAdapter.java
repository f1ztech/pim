package ru.mipt.pim.server.mail;

import java.io.IOException;


public interface OAuthAdapter {

	boolean isHasCredential() throws IOException, MailException;

	void makeCredential(String authCode) throws IOException, MailException;

	String getAuthenticateUrl() throws MailException;

}
