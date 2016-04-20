package ru.mipt.pim.server.model;

import java.util.Date;

import javax.persistence.Entity;

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
@RdfsClass("pim:EmailFolder")
public class EmailFolder extends Resource implements Indexable {

	@RdfProperty("pim:folderId")
	private String folderId;

	@RdfProperty("pim:lastSynchronizedEmailDate")
	private Date lastSynchronizedEmailDate;

	public EmailFolder() {
	}

	public EmailFolder(String name, String folderId) {
		this.folderId = folderId;
		setName(name);
		setTitle(name);
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public Date getLastSynchronizedEmailDate() {
		return lastSynchronizedEmailDate;
	}

	public void setLastSynchronizedEmailDate(Date lastSynchronizedEmailDate) {
		this.lastSynchronizedEmailDate = lastSynchronizedEmailDate;
	}

}
