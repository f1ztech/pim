package ru.mipt.pim.server.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.comparators.NullComparator;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ru.mipt.pim.util.Utilities;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nao", "http://www.semanticdesktop.org/ontologies/2007/08/15/nao/#",
	"nie", "http://www.semanticdesktop.org/ontologies/2007/01/19/nie/",
	"purl", "http://purl.org/dc/elements/1.1/"
})
@RdfsClass("pim:Resource")
@JsonSerialize
public class Resource extends ObjectWithRdfId implements Comparable<Resource> {

	@JsonIgnore
	@RdfProperty("pim:owner")
	@OneToOne(fetch = FetchType.LAZY)
	private User owner;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Tag> tags = new ArrayList<>();

	@RdfProperty("nao:prefLabel")
	private String title;

	@RdfProperty("dc:title")
	private String name;

	@RdfProperty("nao:created")
	private Date dateCreated;

	@RdfProperty("nao:modified")
	private Date dateModified;

	@RdfProperty("nie:htmlContent")
	private String htmlContent;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@RdfProperty("skos:narrower")
	private List<Resource> narrowerResources = new ArrayList<>();

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@RdfProperty("skos:related")
	private List<Resource> relatedResources = new ArrayList<>();

	@RdfProperty("pim:activationValue")
	private BigDecimal activationValue;

	private boolean includeNarrowerResourcesToJson = false;

	@RdfProperty("pim:hasNarrowerResources")
	private boolean hasNarrowerResources;
	
	@RdfProperty("purl:language")
	private String language;

	@JsonProperty("tags")
	public List<Tag> getTags() {
		if (tags == null) {
			tags = new ArrayList<>();
			CollectionUtils.union(getRelatedResources(), getBroaderResources()).forEach(resource -> {
				if (resource.getRealClass().equals(Tag.class)) {
					tags.add((Tag) resource);
				}
			});
		}
		return tags;
	}

	public List<Resource> getNarrowerResources() {
		hasNarrowerResources = !narrowerResources.isEmpty();
		return narrowerResources;
	}

	@JsonProperty(value = "narrowerResources")
	public List<Resource> getPreloadedNarrowerResources() {
		return includeNarrowerResourcesToJson ? getNarrowerResources() : null;
	}

	@JsonIgnore
	public List<Resource> getBroaderResources() {
		return getInvertedList(Resource.class, Resource.class, "narrowerResources");
	}

	@JsonProperty("icon")
	public String getIcon() {
		return Utilities.getIcon(getRealClass());
	}

	@JsonProperty("resourceType")
	public ResourceType getResourceType() {
		return ResourceType.findByClass(getRealClass());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Resource> getRelatedResources() {
		return relatedResources;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public boolean isIncludeNarrowerResourcesToJson() {
		return includeNarrowerResourcesToJson;
	}

	public void setIncludeNarrowerResourcesToJson(boolean includeNarrowerResourcesToJson) {
		this.includeNarrowerResourcesToJson = includeNarrowerResourcesToJson;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	public BigDecimal getActivationValue() {
		return activationValue;
	}

	public void setActivationValue(BigDecimal activationValue) {
		this.activationValue = activationValue;
	}

	public Boolean getHasNarrowerResources() {
		return hasNarrowerResources;
	}

	public void setHasNarrowerResources(Boolean hasNarrowerResources) {
		this.hasNarrowerResources = hasNarrowerResources;
	}

	@Override
	public int compareTo(Resource o) {
		NullComparator<String> nullComparator = new NullComparator<String>();
		return Comparator.comparing(Resource::getTitle, nullComparator).thenComparing(Resource::getName, nullComparator).thenComparing(Resource::getId, nullComparator).compare(this, o);
	}

	@Override
	public Class<? extends Resource> getRealClass() {
		return (Class<? extends Resource>) super.getRealClass();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
