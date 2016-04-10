package ru.mipt.pim.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

import ru.mipt.pim.server.index.Indexable;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nfo",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",
	"pimo", "http://www.semanticdesktop.org/ontologies/2007/11/01/pimo/#",
	"nmo",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nmo/#",
	"nco",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nco/#"
})
@RdfsClass("nmo:Message")
public class Message extends Resource implements Indexable {

	@RdfProperty("nmo:messageId")
	private String messageId;

	@RdfProperty("nmo:from")
	@OneToOne(fetch = FetchType.LAZY)
	private Contact from;

	@RdfProperty("nmo:recipient")
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Contact> recipients = new ArrayList<>();

	@RdfProperty("nmo:secondaryRecipient")
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Contact> secondaryRecipients = new ArrayList<>();

	@RdfProperty("nmo:hasAttachment")
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<File> attachments = new ArrayList<>();

	@RdfProperty("nmo:inReplyTo")
	@OneToOne(fetch = FetchType.LAZY)
	private Message inReplyTo;

	@RdfProperty("nmo:isRead")
	private boolean isRead;

	@RdfProperty("nmo:sentDate")
	private Date sentDate;

	@RdfProperty("nmo:receivedDate")
	private Date receivedDate;

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

	public List<Contact> getSecondaryRecipients() {
		return secondaryRecipients;
	}

	public void setSecondaryRecipients(List<Contact> secondaryRecipients) {
		this.secondaryRecipients = secondaryRecipients;
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

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public List<Contact> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<Contact> recipients) {
		this.recipients = recipients;
	}
}
