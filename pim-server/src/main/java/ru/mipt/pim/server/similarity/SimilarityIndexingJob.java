package ru.mipt.pim.server.similarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.index.IndexFinder;
import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.repositories.UserRepository;

@Component
public class SimilarityIndexingJob {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private SimilarityService similarityService;
	
	@Autowired
	private IndexFinder indexFinder;
	
	@Autowired
	private ResourceRepository resourceRepository;
	
	@Scheduled(cron = "0 0 1 * * *") // every day
	public void indexSimilarity() {
		for (User user : userRepository.findAll()) {
			try {
				storeSimilarityHashes(user);
				storeSimilarResources(user);
			} catch (Exception e) {
				logger.error("Error while refreshing similarity for user " + user.getId(), e);
			}
		}
	}

	private void storeSimilarityHashes(User user) throws Exception {
		for (String resourceId : indexFinder.findAllIndexedResourceIds(user)) {
			Resource resource = resourceRepository.findById(resourceId);
			
			long[] hashes = similarityService.getSimilarityHashes(resource);
			indexingService.storeSimilarityHashes(resource, hashes);
		}
	}
	
	private void storeSimilarResources(User user) throws Exception {
		for (String resourceId : indexFinder.findAllIndexedResourceIds(user)) {
			Resource resource = resourceRepository.findById(resourceId);
			
			resource.getRelatedResources().clear();
			resource.getRelatedResources().addAll(similarityService.getSimilarResources(resource));
		}
	}

}
