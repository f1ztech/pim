package ru.mipt.pim.server.publications;

import static org.joox.JOOX.$;

import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.joox.Match;

public class TEIParser {

	public Article parseTei(String tei) throws ru.mipt.pim.server.publications.ParseException {
		return parseTei(tei, null);
	}
	
	public Article parseTei(String tei, Article article) throws ru.mipt.pim.server.publications.ParseException {
		try {
			article = article == null ? new Article() : article;
			Match $document = $($(tei).document()).namespace("x", "http://www.tei-c.org/ns/1.0");
			Match $header = $document.xpath("//x:teiHeader");
			
			if (!$header.children().isEmpty()) {
				Match $fileDesc = $header.xpath(".//x:fileDesc");
				Match $profileDesc = $header.xpath(".//x:profileDesc");
				
				parseBibliography(article, $fileDesc.xpath(".//x:biblStruct"));
				
				for (Match $term : $header.xpath(".//x:keywords//x:term").each()) {
					article.getKeywors().add($term.text());
				}
				
				article.setAbstractText($profileDesc.xpath(".//x:abstract").text());
			}
			
			for (Match $refBibl : $document.xpath(".//x:listBibl//x:biblStruct").each()) {
				Article reference = new Article();
				parseBibliography(reference, $refBibl);
				article.getReferences().add(reference);
			}
			
			return article;
		} catch (Exception e) {
			throw new ru.mipt.pim.server.publications.ParseException(e);
		}
	}

	private void parseBibliography(Article article, Match $bibl) throws ParseException {
		Match $analytic = $bibl.xpath(".//x:analytic");
		Match $monogr = $bibl.xpath(".//x:monogr");
		Match $series = $bibl.xpath(".//x:series");
		
		parseTitleAndAuthors(article, !$analytic.isEmpty() ? $analytic : !$monogr.isEmpty() ? $monogr : $series);
		
		if (!$monogr.isEmpty()) {
			parseMonogr(article, $monogr);
		}
	}

	private static void parseTitleAndAuthors(Article article, Match $analytic) {
		if ($analytic.find("title").isNotEmpty()) {
			article.setTitle($analytic.find("title").text());
		}
		
		for (Match $author : $analytic.xpath(".//x:author").each()) {
			Author author = new Author();
			author.setFirstName($author.xpath(".//x:persName//x:forename[@type='first']").text());
			author.setMiddleName($author.xpath(".//x:persName//x:forename[@type='middle']").text());
			author.setLastName($author.xpath(".//x:persName//x:surname").text());

			article.getAuthors().add(author);
		}
	}

	private void parseMonogr(Article article, Match $monogr) throws ParseException {
		article.setPublishedIn($monogr.find("title").text());
		String w3cDate = $monogr.xpath(".//x:imprint//x:date[@type='published']").attr("when");
		if (w3cDate != null) {
			article.setDate(new W3CDateFormat().parse(w3cDate));
			Calendar cal = Calendar.getInstance();
			cal.setTime(article.getDate());
			article.setYear(cal.get(Calendar.YEAR));
		}
		if ($monogr.xpath(".//x:imprint//x:publisher").isNotEmpty()) {
			article.setPublisher($monogr.xpath(".//x:imprint//x:publisher").text());
		}
		Match $pages = $monogr.xpath(".//x:imprint//x:biblScope[unit='page']");
		if ($pages.isNotEmpty()) {
			article.setPages(StringUtils.defaultIfEmpty($pages.attr("from"), "") + " - " + StringUtils.defaultIfEmpty($pages.attr("to"), ""));
		}
	}
}
