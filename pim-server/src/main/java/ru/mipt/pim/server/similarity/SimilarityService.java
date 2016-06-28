package ru.mipt.pim.server.similarity;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.index.IndexFinder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.repositories.ResourceRepository;

@Service
public class SimilarityService {

	@Autowired
	@Qualifier("title")
	private LshStrategy titleHashingStrategy;
	
	@Autowired
	@Qualifier("content")
	private LshStrategy contentHashingStrategy;
	
	@Autowired
	@Qualifier("structure")
	private LshStrategy structureHashingStrategy;
	
	@Autowired
	private IndexFinder indexFinder;
	
	@Autowired
	private ResourceRepository resourceRepository;
	
	public long[] getSimilarityHashes(Resource resource) throws Exception {
		long[] hashes = {};

		ArrayUtils.addAll(hashes, titleHashingStrategy.generateHashes(resource));
		ArrayUtils.addAll(hashes, contentHashingStrategy.generateHashes(resource));
		ArrayUtils.addAll(hashes, structureHashingStrategy.generateHashes(resource));
		
		return hashes;
	}

	public List<Resource> getSimilarResources(Resource resource) throws Exception {
		long[] hashes = indexFinder.getSimilarityHashes(resource);
		if (hashes == null) {
			hashes = getSimilarityHashes(resource);
		}
	
		List<Resource> possibleResources = indexFinder
				.findSimilarHashResources(resource, hashes, 100).stream()
				.map(resourceRepository::findById).collect(Collectors.toList());
		
		return possibleResources;
	}
	
}
