package ru.mipt.pim.server.similarity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mipt.pim.server.index.TermsStatisticsService;
import ru.mipt.pim.server.index.TermsStatisticsService.TermsStatistics;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

public abstract class SimHashLshStrategy implements LshStrategy {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private TermsStatisticsService termsStatisticsService;
	
	private Map<User, SimHash> simHashes = new HashMap<>();

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
		SimHash simHash = simHashes.get(user);
		TermsStatistics statistics = termsStatisticsService.getStatistics(user, language);

		if (simHash == null || statistics.getTotalTermsCount() != simHash.get)
		SuperBit superBit;

		File serializedSuperBitFile = new File(fileStorageService.getAbsolutePath(superBitFileName));
		if (serializedSuperBitFile.exists()) {
			try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedSuperBitFile))) {
				superBit = (SuperBit) objectInputStream.readObject();
			}
		} else {
			superBit = new SuperBit(spaceDimension);
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializedSuperBitFile))) {
				objectOutputStream.writeObject(superBit);
			} 
		}
		
		return new SimHash(superBit, stagesCount);
	}

	@Override
	public long[] generateHashes(Resource resource) throws Exception {
		RealVector tfIdf = termsStatisticsService.getTfIdf(resource, indexField);
		return getSimHash(resource.getOwner(), resource.getLanguage()).generateHashes(tfIdf);
	}

}
