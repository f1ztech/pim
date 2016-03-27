package ru.mipt.pim.server.publications;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Article {

	private String title;
	private List<Author> authors = new ArrayList<>();
	private List<String> keywors = new ArrayList<>();
	private String abstractText;
	private int year;
	private Date date;
	private String publishedIn;
	private String publisher;
	private String pages;
	private List<Article> references = new ArrayList<>();

	public Article() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public List<String> getKeywors() {
		return keywors;
	}

	public void setKeywors(List<String> keywors) {
		this.keywors = keywors;
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

	public List<Article> getReferences() {
		return references;
	}

	public void setReferences(List<Article> references) {
		this.references = references;
	}

	public String getPublishedIn() {
		return publishedIn;
	}

	public void setPublishedIn(String publishedIn) {
		this.publishedIn = publishedIn;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}
}
