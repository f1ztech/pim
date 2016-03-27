package ru.mipt.pim.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nfo",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",
	"pimo", "http://www.semanticdesktop.org/ontologies/2007/11/01/pimo/#",
	"ncal", "http://www.semanticdesktop.org/ontologies/2007/04/02/ncal/#"
})
@RdfsClass("pim:UserConfigs")
public class UserConfigs extends ObjectWithRdfId {

	@RdfProperty("pim:oauthEmailUser")
	private String oauthEmailUser;

	@RdfProperty("pim:synchronizedEmailFolders")
	@ManyToMany(fetch = FetchType.LAZY)
	private List<EmailFolder> synchronizedEmailFolders = new ArrayList<>();

	public List<EmailFolder> getSynchronizedEmailFolders() {
		return synchronizedEmailFolders;
	}

	public void setSynchronizedEmailFolders(List<EmailFolder> synchronizedEmailFolders) {
		this.synchronizedEmailFolders = synchronizedEmailFolders;
	}

	public String getOauthEmailUser() {
		return oauthEmailUser;
	}

	public void setOauthEmailUser(String oauthEmailUser) {
		this.oauthEmailUser = oauthEmailUser;
	}

}
