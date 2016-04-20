package ru.mipt.pim.server.mail;

import java.util.Date;
import java.util.List;

import ru.mipt.pim.server.model.EmailFolder;

public interface MailAdapter {

	List<MessageFolder> getFolders() throws MailException;

	MessageQueryResults findNewMessages(EmailFolder folder, Date fromDate, MessageQueryResults lastResults) throws MailException;

}
