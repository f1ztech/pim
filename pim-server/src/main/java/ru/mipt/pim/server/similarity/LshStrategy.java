package ru.mipt.pim.server.similarity;

import ru.mipt.pim.server.model.Resource;

public interface LshStrategy {

	long[] generateHashes(Resource resource) throws Exception;
	
}
