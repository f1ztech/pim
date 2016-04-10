package ru.mipt.pim.server.model;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;

import ru.mipt.pim.server.index.Indexable;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nfo",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",
	"pimo", "http://www.semanticdesktop.org/ontologies/2007/11/01/pimo/#"
})
@RdfsClass("pimo:Project")
public class Project extends Resource implements Indexable {

}
