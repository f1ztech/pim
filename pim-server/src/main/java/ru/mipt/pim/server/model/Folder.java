package ru.mipt.pim.server.model;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nfo", "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#"
})
@RdfsClass("pim:Folder")
@JsonSerialize
public class Folder extends Tag {

	@RdfProperty("pim:path")
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
