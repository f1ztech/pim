package ru.mipt.pim.server.mail;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.apache.commons.lang3.ObjectUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.model.Contact;
import ru.mipt.pim.server.model.Email;
import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.model.UserConfigs;
import ru.mipt.pim.server.repositories.ContactRepository;
import ru.mipt.pim.server.repositories.EmailRepository;
import ru.mipt.pim.server.repositories.EmailFolderRepository;
import ru.mipt.pim.server.repositories.UserRepository;
import ru.mipt.pim.server.services.RepositoryService;

@Component
public class MailSynchronizer {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private UserRepository userRepository;

	@Resource
	private ContactRepository contactRepository;

	@Resource
	private EmailFolderRepository emailFolderRepository;

	@Resource
	private EmailRepository emailRepository;
	
	@Resource
	private IndexingService indexingService;

	@Resource
	private MailAdapterService mailAdapterService;
	
	@Autowired
	private RepositoryService repositoryService;

	@Scheduled(fixedDelay = 1 * 60 * 1000) // every 10 minutes
	public void synchronizeMail() throws IOException, MailException, MessagingException {
		for (User user : userRepository.findByNotNullOauthEmailUser()) {
			try {
				synchronizeUserMails(user);
			} catch (Exception e) {
				logger.error("Error while synchornizing mails for user " + user.getId(), e);
			}
		}
	}

	@Async
	public void triggerSynchronization(User user) throws Exception {
		synchronizeUserMails(user);
	}
	
	private void synchronizeUserMails(User user) throws Exception {
		synchronized (user.getId().intern()) { // TODO bad synchronization
			MailAdapter mailAdapter = mailAdapterService.getAdapter(user);
			if (mailAdapter != null) {
				for (EmailFolder folderToSync : user.getUserConfigs().getSynchronizedEmailFolders()) {
					try {
						synchronizeFolder(user, mailAdapter, folderToSync);
					} catch (Exception e) {
						logger.error("Error while synchornizing folder " + folderToSync.getId(), e);
					}
				}
			}
		}
	}

	private void synchronizeFolder(User user, MailAdapter mailAdapter, EmailFolder folderToSync) throws Exception {
		EmailFolder folder = emailFolderRepository.findByFolderId(user, folderToSync.getFolderId());
		if (folder == null) {
			folder = new EmailFolder();
			folder.setTitle(folderToSync.getName());
			folder.setName(folderToSync.getName());
			folder.setOwner(user);
			folder.setFolderId(folderToSync.getFolderId());
			emailFolderRepository.save(folder);
		}
		
		Set<String> existedMessageIds = emailRepository.getMessageIds(user, folderToSync);

		MessageQueryResults lastResults = null;
		Date lastSynchronizedDate = null;
		while (true) {
			MessageQueryResults newResults = mailAdapter.findNewMessages(folder, folder.getLastSynchronizedEmailDate(), lastResults);
			if (lastResults == null && !newResults.getMessages().isEmpty()) { // FIXME message may be moved from another folder
				lastSynchronizedDate = newResults.getMessages().get(0).getInternalReceivedDate();
			}

			for (Message newMessage : newResults.getMessages()) {
				if (existedMessageIds.contains(newMessage.getMessageId())) {
					continue;
				}
				
				Email email = new Email();
				email.setFrom(findOrCreateContact(user, newMessage.getFrom()));
				email.setSentDate(newMessage.getSentDate());
				email.setReceivedDate(newMessage.getReceivedDate());
				email.setThreadId(newMessage.getThreadId());
				email.setMessageId(newMessage.getMessageId());
				email.setHtmlContent(newMessage.getBody());
				email.getRecipients().addAll(findOrCreateContacts(user, newMessage.getRecipients()));
				email.getSecondaryRecipients().addAll(findOrCreateContacts(user, newMessage.getCc()));
				email.setTitle(newMessage.getSubject());
				email.setOwner(user);
				emailRepository.save(email);
				repositoryService.addNarrowerResource(folder, email);
			}
			
			if (!newResults.isHasMore()) {
				break;
			}
			lastResults = newResults;
		}
		
		Date finalDate = lastSynchronizedDate;
		if (lastSynchronizedDate != null) {
			repositoryService.setProperty(folder, "pim:lastSynchronizedEmailDate", f -> f.createLiteral(finalDate));
		}
	}

	private List<Contact> findOrCreateContacts(User user, List<Message.Contact> contacts) throws MessagingException {
		return contacts.stream().map(c -> findOrCreateContact(user, c)).collect(Collectors.toList());
	}

	private Contact findOrCreateContact(User user, Message.Contact contact) {
		Contact storedContact = contactRepository.findByEmail(user, contact.getEmail());
		if (storedContact == null) {
			storedContact = new Contact();
			storedContact.setEmail(contact.getEmail());
			storedContact.setTitle(ObjectUtils.defaultIfNull(contact.getTitle(), contact.getEmail()));
			storedContact.setOwner(user);
			contactRepository.save(storedContact);
		}
		return storedContact;
	}

}
