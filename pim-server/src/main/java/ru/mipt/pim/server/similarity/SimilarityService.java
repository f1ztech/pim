package ru.mipt.pim.server.similarity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.index.IndexingService;

@Service
public class SimilarityService {

	@Autowired
	private IndexingService indexingService;
	
	
	
}
