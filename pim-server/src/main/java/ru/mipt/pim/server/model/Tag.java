package ru.mipt.pim.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Namespaces({
	"pim",  "http://mipt.ru/pim/",
	"nao", "http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#"
})
@RdfsClass("nao:Tag")
public class Tag extends Resource {
	
	private List<Resource> taggedResoruces; 

	@JsonIgnore
	public List<Resource> getTaggedResources() {
		if (taggedResoruces == null) {
			taggedResoruces = new ArrayList<>(getNarrowerResources());
			taggedResoruces.stream().filter(resource -> {
				return resource instanceof Tag;
			});
		}
		return taggedResoruces;
	}

}
