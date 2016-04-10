package ru.mipt.pim.server.similarity;

import ru.mipt.pim.server.model.ObjectWithRdfId;
import ru.mipt.pim.server.model.User;

public interface LshStrategy {

	long[] generateHashes(User user, ObjectWithRdfId resource) throws Exception;
	
}
