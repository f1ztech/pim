package ru.mipt.pim.server.similarity;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.ObjectWithRdfId;
import ru.mipt.pim.server.model.User;

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
	
	public long[] getSimilarityHashes(User user, ObjectWithRdfId resource) throws Exception {
		long[] hashes = {};

		ArrayUtils.addAll(hashes, titleHashingStrategy.generateHashes(user, resource));
		ArrayUtils.addAll(hashes, contentHashingStrategy.generateHashes(user, resource));
		ArrayUtils.addAll(hashes, structureHashingStrategy.generateHashes(user, resource));
		
		return hashes;
	}

}
