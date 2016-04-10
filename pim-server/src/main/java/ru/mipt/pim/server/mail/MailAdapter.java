package ru.mipt.pim.server.mail;

import java.util.Date;
import java.util.List;

import ru.mipt.pim.server.model.EmailFolder;

public interface MailAdapter {

	List<MessageFolder> getFolders() throws MailException;

	List<Message> findNewMessages(EmailFolder folder, Date fromDate) throws MailException;

}
