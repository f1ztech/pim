package ru.mipt.pim.server.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Publication;
import ru.mipt.pim.server.model.Tag;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.TagRepository;

@Component
public class RecommendationService {

	private static final int CONTENT_MULTIPLIER = 1;
	private static final int TITLE_MULTIPLIER = 1;
	private static final int ABSTRACT_MULTIPLIER = 1;

	@Resource
	private IndexingService indexingService;

	@Resource
	private TagRepository tagRepository;

	public static boolean useTitles = false;
	public static boolean useAbstracts = false;
	public static int power = 8;

	private Map<String, List<Object[]>> termScores;
	private Map<Tag, List<Object[]>> tagScores;
	private Map<String, Float> termTfIdfs;

	private float computeWordTagWeight(IndexReader reader, String term, Tag tag, int... excludeDocs) throws IOException {
		float weight = 0;
		List<Integer> contentDocuments = indexingService.findByTagAndContent(reader, tag.getId(), term);

		HashSet<Integer> allDocuments = new HashSet<>();
		allDocuments.addAll(contentDocuments);

		List<Integer> titleDocuments = null;
		if (useTitles) {
			titleDocuments = indexingService.findByTagAndTitle(reader, tag.getId(), term);
			allDocuments.addAll(titleDocuments);
		}
		List<Integer> abstractDocuments = null;
		if (useAbstracts) {
			abstractDocuments = indexingService.findByTagAndAbstract(reader, tag.getId(), term);
		}

		for (Integer docId : allDocuments) {
			if (!ArrayUtils.contains(excludeDocs, docId)) {

				float multiplier = contentDocuments.contains(docId) ? CONTENT_MULTIPLIER : 0;
				if (useTitles) {
					multiplier += titleDocuments.contains(docId) ? TITLE_MULTIPLIER : 0;
				}
				if (useAbstracts) {
					multiplier += abstractDocuments.contains(docId) ? ABSTRACT_MULTIPLIER : 0;
				}

				weight += multiplier * indexingService.computeTfIdf(reader, term, docId);
			}
		}
		return allDocuments.isEmpty() ? 0 : weight / allDocuments.size();
	}

	private float computeResourceTagWeight(IndexReader reader, int docId, Terms terms, Tag tag) throws IOException {
		TermsEnum it = terms.iterator();

		float weight = 0;
		BytesRef term;
		while ((term = it.next()) != null) {
			String strTerm = term.utf8ToString();
			if (strTerm.length() > 2) {
//				long start = System.currentTimeMillis();
				float tfIdf = indexingService.computeTfIdf(reader, strTerm, docId);

				if (tfIdf < 0.07) {
					continue;
				}

//				termTfIdfs.put(strTerm, tfIdf);
				float wtw = computeWordTagWeight(reader, strTerm, tag, docId);
//				if (!termScores.containsKey(strTerm)) {
//					termScores.put(strTerm, new ArrayList<>());
//				}
				float resourceWeight = (float) (Math.pow(tfIdf, power) * wtw);
//				termScores.get(strTerm).add(new Object[] {tag, resourceWeight});

//				if (!tagScores.containsKey(tag)) {
//					tagScores.put(tag, new ArrayList<>());
//				}
//				tagScores.get(tag).add(new Object[] {strTerm, resourceWeight});
//					System.out.println(
//							(System.currentTimeMillis() - start) + "\n"
//							+ "TFIDF(" + term.utf8ToString() + ", " + reader.document(docId).get("id") + ") = " + tfIdf + "\n"
//							+ "WT(" + term.toString() + ", " + tag.getTitle() + ") = " + wtw + "\n");
				weight += resourceWeight;
			}
		}
		return weight;
	}

	public List<Tag> getRecommendations(User user, ru.mipt.pim.server.model.Resource resource) throws IOException {
		long start = System.currentTimeMillis();

		if (resource instanceof Publication) {
			resource = ((Publication) resource).getPublicationFile();
		}

		DirectoryReader reader = indexingService.createIndexReader(user);
//		System.out.println("open");
//		System.out.println(System.currentTimeMillis() - start);
		try {
			indexingService.countTermsSec = 0;
			indexingService.seekDocSes = 0;
			indexingService.clearDocTerms();
			termScores = new HashMap<>();
			tagScores = new HashMap<>();
			termTfIdfs = new HashMap<>();
			List<Tag> recommendedTags = new ArrayList<Tag>();
			Integer docId = indexingService.findDocIdByResourceId(resource.getId(), reader);
			if (docId != null) {
				HashMap<Tag, Float> weightsMap = new HashMap<>();
				Terms terms = indexingService.getContentTerms(reader, docId);
				if (terms == null) {
					return Collections.emptyList();
				}
				for (Tag tag : tagRepository.findAll(user)) {
					float weight = computeResourceTagWeight(reader, docId, terms, tag);
					weightsMap.put(tag, weight);
				}

//				Map<String, List<Object[]>> sortedScores = new HashMap<String, List<Object[]>>();
//				termScores.entrySet().forEach(entry -> {
//					List<Object[]> sortedVals = entry.getValue().stream().sorted((arr1, arr2) -> -1 * Float.compare((float) arr1[1], (float) arr2[1])).collect(Collectors.toList());
//					sortedScores.put(entry.getKey(), sortedVals);
//				});
//				sortedScores.entrySet().stream().sorted((entry1, entry2) -> {
//					return -1 * Float.compare((float) entry1.getValue().get(0)[1], (float) entry2.getValue().get(0)[1]);
//				}).forEach(entry -> {
//					List<Object[]> sortedVals = entry.getValue();
//					System.out.println(entry.getKey() + " (" + termTfIdfs.get(entry.getKey()) + ")");
//
//					if (sortedVals.size() > 0) {
//						System.out.println(((Tag)sortedVals.get(0)[0]).getTitle() + " " + sortedVals.get(0)[1] + " || total: " + weightsMap.get((Tag)sortedVals.get(0)[0]));
//					}
//					if (sortedVals.size() > 1) {
//						System.out.println(((Tag)sortedVals.get(1)[0]).getTitle() + " " + sortedVals.get(1)[1]+ " || total: " + weightsMap.get((Tag)sortedVals.get(1)[0]));
//					}
//					System.out.println();
//				});
				recommendedTags.addAll(weightsMap.keySet());
				recommendedTags.sort((t1, t2) -> -1 * Float.compare(weightsMap.get(t1), weightsMap.get(t2)));
//
//				for (int i = 0; i < 2; i++) {
//					printTagInfo(recommendedTags.get(i));
//				}
//
//				printTagInfo((Tag) resource.getBroaderResources().get(0));

//				System.out.println("countTermsSec");
//				System.out.println(indexingService.countTermsSec / 1000000);
//				System.out.println("seekDocSes");
//				System.out.println(indexingService.seekDocSes / 1000000);
//				System.out.println("termVectorSec");
//				System.out.println(indexingService.termVectorSec / 1000000);
//				System.out.println("querySec");
//				System.out.println(indexingService.querySec / 1000000);

			}
			return recommendedTags;
		} finally {
			start = System.currentTimeMillis();
			reader.close();
//			System.out.println("close");
//			System.out.println(System.currentTimeMillis() - start);
		}
	}

	private void printTagInfo(Tag tag) {
		List<Object[]> terms = tagScores.get(tag);
		terms.sort((arr1, arr2) -> -1 * Float.compare((float) arr1[1], (float) arr2[1]));
		terms.forEach(arr -> {
			System.out.println(arr[0] + " (" + termTfIdfs.get(arr[0]) + ")" + " - " + arr[1]);
		});
	}
}
