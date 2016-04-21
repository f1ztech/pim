package ru.mipt.pim.server.mail;

import java.util.Date;
import java.util.List;
import java.util.Set;

import ru.mipt.pim.server.model.EmailFolder;

public interface MailAdapter {

	List<MessageFolder> getFolders() throws MailException;

	MessageQueryResults findNewMessages(EmailFolder folder, Date fromDate, MessageQueryResults lastResults, Set<String> existedMessageIds) throws MailException;

}
