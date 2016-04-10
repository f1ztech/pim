package ru.mipt.pim.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ru.mipt.pim.server.index.Indexable;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc", "http://purl.org/dc/terms/",
	"pim", "http://mipt.ru/pim/",
	"fabio", "http://purl.org/spar/fabio/",
	"prism", "http://prismstandard.org/namespaces/basic/2.0/",
	"cito", "http://purl.org/spar/cito/" })
@RdfsClass("pim:Publication")
@JsonSerialize
public class Publication extends Resource implements Indexable {

	@RdfProperty("pim:publicationFile")
	private File publicationFile;

	@RdfProperty("dc:creator")
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Person> authors = new ArrayList<>();

	@RdfProperty("dc:abstract")
	private String abstractText;

	@RdfProperty("dc:publisher")
	private String publisher;

	@RdfProperty("prism:pageRange")
	private String pageRange;

	@RdfProperty("prism:publicationName")
	private String publicationName;

	@RdfProperty("prism:keyword")
	private List<String> keywords = new ArrayList<>();

	@RdfProperty("prism:publicationDate")
	private Date publicationDate;

	@RdfProperty("fabio:year")
	private Integer year;

	@RdfProperty("cito:cites")
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Publication> cites = new ArrayList<>();

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<Person> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Person> authors) {
		this.authors = authors;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPageRange() {
		return pageRange;
	}

	public void setPageRange(String pageRange) {
		this.pageRange = pageRange;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public List<Publication> getCites() {
		return cites;
	}

//	@JsonProperty("cites")
//	public List<String> getCitesStrings() {
//		List<String> citesStr = cites.stream().map(p -> p.getTitle()).filter(s -> s != null).collect(Collectors.toList());
//		return citesStr.size() > 5 ? citesStr.subList(0, 4) : citesStr;
//	}

	public void setCites(List<Publication> cites) {
		this.cites = cites;
	}

	public File getPublicationFile() {
		return publicationFile;
	}

	public void setPublicationFile(File publicationFile) {
		this.publicationFile = publicationFile;
	}

}
