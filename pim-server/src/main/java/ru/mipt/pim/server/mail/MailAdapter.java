package ru.mipt.pim.server.mail;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import ru.mipt.pim.server.model.EmailFolder;

public interface MailAdapter {

	List<MessageFolder> getFolders() throws MailException, IOException;

	List<Message> findNewMessages(EmailFolder folder, Date fromDate) throws MailException, IOException, MessagingException;

}
