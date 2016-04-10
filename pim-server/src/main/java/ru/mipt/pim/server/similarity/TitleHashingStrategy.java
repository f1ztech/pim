package ru.mipt.pim.server.similarity;

import org.springframework.stereotype.Component;

import ru.mipt.pim.server.index.IndexingService;

@Component(value="title")
public class TitleHashingStrategy extends SimHashLshStrategy {

	private static final int LSH_STAGES_COUNT = 15;
	private static final int LSH_SIGNATURE_DIMENSION = 10 * LSH_STAGES_COUNT;
	
	public TitleHashingStrategy() {
		super(LSH_STAGES_COUNT, LSH_SIGNATURE_DIMENSION, "titleHashingSuperBit", IndexingService.TITLE_FIELD);
	}

}
