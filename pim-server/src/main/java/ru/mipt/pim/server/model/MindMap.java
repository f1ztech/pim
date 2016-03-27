package ru.mipt.pim.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

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
@RdfsClass("pim:MindMap")
public class MindMap extends Resource {

	@RdfProperty("pim:rootNode")
	@OneToOne(fetch=FetchType.LAZY)
	private MindMapNode rootNode;

	public MindMapNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(MindMapNode rootNode) {
		this.rootNode = rootNode;
	}

}
