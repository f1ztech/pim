package ru.mipt.pim.server.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import ru.mipt.pim.server.mail.Message.Contact;
import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.User;

public class GmailAdapter implements MailAdapter, OAuthAdapter {

	static final String CLIENT_ID = "33219966533-0bf1l6o2nlq8rv2pv1vsljgh1ii6daba.apps.googleusercontent.com";
	static final String CLIENT_SECRET = "dEl_Ops5H5bbr1oYRRHyPreA";
	static final String CALLBACK_URI = "http://localhost:8080/mail/oauthCallback";

	static final Collection<String> SCOPE = Collections.singletonList(GmailScopes.GMAIL_READONLY);
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	private Log logger = LogFactory.getLog(getClass());

	private GoogleAuthorizationCodeFlow flow;
	private String stateToken;
	private Credential credential;
	private String userId;
	private Gmail service;

	public GmailAdapter(User user, AbstractDataStoreFactory dataStoreFactory, MailAdapterService mailAdapterService) throws IOException {
		this.userId = user.getUserConfigs().getOauthEmailUser();
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE)
				.setAccessType("offline")
				.setDataStoreFactory(dataStoreFactory)
				.addRefreshListener(new DataStoreCredentialRefreshListener(userId, dataStoreFactory))
				.build();
		credential = flow.loadCredential(userId);
		makeGmailService();
		generateStateToken();
	}

	private void makeGmailService() {
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("MemoPIM").build();
	}

	// ========================================================
	// oauth2 authentication
	// ========================================================

	public String getAuthenticateUrl() {
		final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		return url.setRedirectUri(CALLBACK_URI).setState(stateToken).setApprovalPrompt("force").build();
	}

	public void makeCredential(String authCode) throws IOException, MailException {
		GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
		credential = flow.createAndStoreCredential(response, userId);
		makeGmailService();
	}

	private void generateStateToken() {
		SecureRandom sr1 = new SecureRandom();
		stateToken = "google;" + sr1.nextInt();
	}

	public boolean isHasCredential() {
		return credential != null;
	}

	// ========================================================
	// query folders
	// ========================================================

	public List<MessageFolder> getFolders() throws MailException {
		try {
			ListLabelsResponse labelsResponse = service.users().labels().list(userId).execute();
			return labelsResponse.getLabels().stream()
//					.filter(l -> !l.getType().equals("system"))
					.map(l -> new MessageFolder(l.getName(), l.getId()))
					.collect(Collectors.toList());
		} catch (IOException e) {
			handleJsonException(e);
			throw new MailException(e);
		}
	}

	private void handleJsonException(Exception e) throws RequireOauthAuthenticationException {
		if ((e instanceof GoogleJsonResponseException) && ((GoogleJsonResponseException) e).getStatusCode() == 401) {
			throw new RequireOauthAuthenticationException();
		}
	}

	// ========================================================
	// query emails
	// ========================================================

	@Override
	public MessageQueryResults findNewMessages(EmailFolder folder, Date fromInternalDate, MessageQueryResults lastResults, Set<String> existedMessageIds) throws MailException {
		try {
			return findMessagesAfter(folder, fromInternalDate, lastResults, existedMessageIds);
		} catch (IOException | MessagingException e) {
			throw new MailException(e);
		}
	}

	private MessageQueryResults findMessagesAfter(EmailFolder folder, Date fromInternalDate, MessageQueryResults lastResults, Set<String> existedMessageIds) throws IOException, MessagingException {
		String nextPageToken = lastResults != null ? lastResults.getAttribute("nextPageToken") : null;
		
		List<Message> newMessages = new ArrayList<>();
		Messages.List query = findMessages(folder).setMaxResults(100l);
		if (nextPageToken != null) {
			query.setPageToken(nextPageToken);
		}

		boolean finalMessageFound = false;
		boolean first = true;
		MessageQueryResults newResults = new MessageQueryResults();
		ListMessagesResponse result = query.execute();
		for (Message message : result.getMessages()) {
			if (existedMessageIds.contains(message.getId()) && !first) {
				continue;
			}
			
			message = service.users().messages().get(userId, message.getId()).setFormat("raw").execute();
			Date messageDate = new Date(message.getInternalDate());
			if (first) {
				newResults.setNewestMessageDate(messageDate);
				first = false;
			}
			if (fromInternalDate != null && !messageDate.after(fromInternalDate)) {
				finalMessageFound = true;
				break;
			} else {
				newMessages.add(message);
			}
		}
		
		if (!finalMessageFound) {
			newResults.setAttribute("nextPageToken", result.getNextPageToken());
			newResults.setHasMore(result.getNextPageToken() != null);
		}
		List<ru.mipt.pim.server.mail.Message> messages = new ArrayList<>(); // convert from gmail message class to ru.mipt.pim.server.mail message class
		for (Message message : newMessages) {
			try {
				messages.add(populateMessage(message, folder));
			} catch (Exception e) {
				logger.error("Error while populating message " + message.getId(), e);
			}
		}
		newResults.setMessages(messages);
		
		return newResults;
	}

	private Messages.List findMessages(EmailFolder folder) throws IOException {
		return findMessages((String) null).setLabelIds(Collections.singletonList(folder.getFolderId()));
	}

	private Messages.List findMessages(String query) throws IOException {
		return service.users().messages().list(userId).setQ(query);
	}

	// ========================================================
	// mapping
	// ========================================================

	private ru.mipt.pim.server.mail.Message populateMessage(Message message, EmailFolder folder) throws MessagingException, IOException {
		Session session = Session.getDefaultInstance(new Properties(), null);
		byte[] emailBytes = Base64.decodeBase64(message.getRaw());
		MimeMessage mimeMessage = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

		ru.mipt.pim.server.mail.Message email = new ru.mipt.pim.server.mail.Message();
		if (mimeMessage.getFrom() != null && mimeMessage.getFrom().length > 0) {
			email.setFrom(new Contact(mimeMessage.getFrom()[0]));
		}
		email.setSentDate(mimeMessage.getSentDate());
		email.setReceivedDate(mimeMessage.getReceivedDate());
		email.setInternalReceivedDate(new Date(message.getInternalDate()));
		email.setThreadId(message.getThreadId());
		email.setMessageId(message.getId());
		email.setSubject(mimeMessage.getSubject());
		email.setBody(getHtmlContent(mimeMessage));
		email.getRecipients().addAll(mapToContacts(getRecipients(mimeMessage, RecipientType.TO)));
		email.getCc().addAll(mapToContacts(getRecipients(mimeMessage, RecipientType.CC)));

		return email;
	}

	private Address[] getRecipients(MimeMessage mimeMessage, RecipientType recipientType) {
		try {
			return mimeMessage.getRecipients(recipientType);
		} catch (MessagingException e) {
			logger.error("Error parsing recepient", e);
		}
		return null;
	}

	private List<Contact> mapToContacts(Address[] addresses) {
		return addresses == null ? Collections.emptyList() : Arrays.stream(addresses).map(Contact::new).collect(Collectors.toList());
	}

	/**
	 * Return the primary text content of the message.
	 */
	private String getHtmlContent(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			return "<pre>" + s + "</pre>";
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = getHtmlContent(bp);
					}
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getHtmlContent(bp);
					if (s != null) {
						return s;
					}
				} else {
					return getHtmlContent(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getHtmlContent(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}
			}
		}

		return null;
	}

}