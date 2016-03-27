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
@RdfsClass("nfo:FileDataObject")
@JsonSerialize
public class File extends Resource {

	@RdfProperty("nfo:hashValue")
	private String hash;

	@RdfProperty("pim:filePath")
	private String path;
	
	@RdfProperty("nfo:fileSize")
	private Long size;
	
	// -- getters/setters

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
}
