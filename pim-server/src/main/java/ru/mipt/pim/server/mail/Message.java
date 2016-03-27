package ru.mipt.pim.server.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import ru.mipt.pim.server.model.File;

public class Message {

	public static class Contact {
		private String email;
		private String title;

		public Contact(Address sender) {
			email = sender instanceof InternetAddress ? ((InternetAddress) sender).getAddress() : sender.toString();
			title = sender instanceof InternetAddress ? ((InternetAddress) sender).getPersonal() : sender.toString();
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	private String messageId;
	private Contact from;
	private List<Contact> recipients = new ArrayList<>();
	private List<Contact> cc = new ArrayList<>();
	private List<File> attachments = new ArrayList<>();
	private Message inReplyTo;
	private boolean isRead;
	private Date sentDate;
	private Date receivedDate;
	private Date internalReceivedDate;
	private String threadId;
	private String subject;
	private String body;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Contact getFrom() {
		return from;
	}

	public void setFrom(Contact from) {
		this.from = from;
	}

	public List<Contact> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<Contact> recipients) {
		this.recipients = recipients;
	}

	public List<Contact> getCc() {
		return cc;
	}

	public void setCc(List<Contact> cc) {
		this.cc = cc;
	}

	public List<File> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}

	public Message getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(Message inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getInternalReceivedDate() {
		return internalReceivedDate;
	}

	public void setInternalReceivedDate(Date internalReceivedDate) {
		this.internalReceivedDate = internalReceivedDate;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
