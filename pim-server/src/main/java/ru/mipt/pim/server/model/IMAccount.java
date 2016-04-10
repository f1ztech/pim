package ru.mipt.pim.server.model;

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
@RdfsClass("nco:IMAccount")
public class IMAccount implements Indexable {

	@RdfProperty("nco:imNickname")
	private String nickName;

	@RdfProperty("nco:imAccountType")
	private String imAccountType;

	public String getImAccountType() {
		return imAccountType;
	}

	public void setImAccountType(String imAccountType) {
		this.imAccountType = imAccountType;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
