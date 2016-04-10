package ru.mipt.pim.server.similarity;

import org.springframework.stereotype.Component;

import ru.mipt.pim.server.index.IndexingService;

@Component(value="content")
public class ContentHashingStrategy extends SimHashLshStrategy {

	private static final int LSH_STAGES_COUNT = 15;
	private static final int LSH_SIGNATURE_DIMENSION = 32 * LSH_STAGES_COUNT;
	
	public ContentHashingStrategy() {
		super(LSH_STAGES_COUNT, LSH_SIGNATURE_DIMENSION, "contentHashingSuperBit", IndexingService.CONTENT_FIELD);
	}
	
}
