package ru.mipt.pim.server.similarity;

import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mipt.pim.server.index.TermsStatisticsService;
import ru.mipt.pim.server.index.TermsStatisticsService.TermsStatistics;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class SimHashLshStrategy implements LshStrategy {

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private TermsStatisticsService termsStatisticsService;

	private Map<User, SimHash> simHashCache = new HashMap<>();

	private int spaceDimension;
	private int stagesCount;

	private String superBitFileName;
	private String indexField;

	public SimHashLshStrategy(int stagesCount, int spaceDimension, String superBitFileName, String indexField) {
		this.stagesCount = stagesCount;
		this.spaceDimension = spaceDimension;
		this.superBitFileName = superBitFileName;
		this.indexField = indexField;
	}

	public SimHash getSimHash(User user, String language) throws Exception {
		SimHash simHash = simHashCache.get(user);
		TermsStatistics statistics = termsStatisticsService.getStatistics(user, language);

		SuperBit superBit = null;
		if (simHash == null || statistics.getTotalTermsCount() != simHash.getDimension()) {
			if (simHash == null) {
				File serializedSuperBitFile = new File(fileStorageService.getAbsolutePath(superBitFileName));
				if (serializedSuperBitFile.exists()) {
					try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedSuperBitFile))) {
						superBit = (SuperBit) objectInputStream.readObject();
					}
				}
			} else {
				superBit = new SuperBit(spaceDimension);
				try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializedSuperBitFile))) {
					objectOutputStream.writeObject(superBit);
				}
			}

			simHash = new SimHash(superBit, stagesCount);
		}
		return simHash;
	}

	@Override
	public long[] generateHashes(Resource resource) throws Exception {
		RealVector tfIdf = termsStatisticsService.getTfIdf(resource, indexField);
		return getSimHash(resource.getOwner(), resource.getLanguage()).generateHashes(tfIdf);
	}

}
