package ru.mipt.pim.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@RdfsClass("nco:Contact")
public class Contact extends Resource implements Indexable {

	@RdfProperty("nco:hasName")
	private String name;

	@RdfProperty("nco:hasEmailAddress")
	private String email;

	@RdfProperty("nco:hasIMAccount")
	private String imAccount;

	@RdfProperty("nco:belongsToGroup")
	@OneToOne(fetch = FetchType.LAZY)
	private ContactGroup group;

	@RdfProperty("nco:photo")
	@OneToOne(fetch = FetchType.LAZY)
	private File photo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ContactGroup getGroup() {
		return group;
	}

	public void setGroup(ContactGroup group) {
		this.group = group;
	}

	public File getPhoto() {
		return photo;
	}

	public void setPhoto(File photo) {
		this.photo = photo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getImAccount() {
		return imAccount;
	}

	public void setImAccount(String imAccount) {
		this.imAccount = imAccount;
	}


}
