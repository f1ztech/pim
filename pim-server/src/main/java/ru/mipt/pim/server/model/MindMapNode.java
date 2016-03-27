package ru.mipt.pim.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

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
@RdfsClass("pim:MindMapNode")
public class MindMapNode extends Resource {

	@RdfProperty("pim:represents")
	private Resource representedResource;

	@RdfProperty("pim:childNode")
	private List<MindMapNode> childNodes = new ArrayList<>();

	@RdfProperty("pim:nodeIndex")
	private int nodeIndex;

	public Resource getRepresentedResource() {
		return representedResource;
	}

	public void setRepresentedResource(Resource representedResource) {
		this.representedResource = representedResource;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public List<MindMapNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<MindMapNode> childNodes) {
		this.childNodes = childNodes;
	}

}
