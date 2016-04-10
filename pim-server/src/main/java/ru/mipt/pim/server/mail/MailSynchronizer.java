package ru.mipt.pim.server.mail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private void synchronizeUserMails(User user) throws IOException, MailException, MessagingException, LangDetectException {
		MailAdapter mailAdapter = mailAdapterService.getAdapter(user);
		if (mailAdapter != null) {
			UserConfigs userConfigs = user.getUserConfigs();
			List<EmailFolder> foldersToSync = userConfigs.getSynchronizedEmailFolders();
			for (EmailFolder folderToSync : foldersToSync) {
				EmailFolder folder = emailFolderRepository.findByFolderId(user, folderToSync.getFolderId());
				if (folder == null) {
					folder = new EmailFolder();
					folder.setTitle(folderToSync.getName());
					folder.setName(folderToSync.getName());
					folder.setOwner(user);
					folder.setFolderId(folderToSync.getFolderId());
					emailFolderRepository.save(folder);
				}

				List<Message> newMessages = mailAdapter.findNewMessages(folder, folder.getLastSynchronizedEmailDate());
				if (!newMessages.isEmpty()) { // FIXME message may be moved from another folder
					folder.setLastSynchronizedEmailDate(newMessages.get(0).getInternalReceivedDate());
				}

				for (Message newMessage : newMessages) {
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
					emailRepository.save(email);
					folder.getNarrowerResources().add(email);
				}
				emailFolderRepository.merge(folder); // FIXME full merge is slow..
			}
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
			storedContact.setTitle(contact.getTitle());
			contactRepository.save(storedContact);
		}
		return storedContact;
	}

}
