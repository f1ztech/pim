package ru.mipt.pim.server.similarity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mipt.pim.server.index.TermsStatisticsService;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.services.FileStorageService;

public abstract class SimHashLshStrategy implements LshStrategy {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private TermsStatisticsService termsStatisticsService;
	
	private SimHash simHash;
	
	private int signatureDimension;
	private int stagesCount;
	
	private String superBitFileName;
	private String indexField;
	
	public SimHashLshStrategy(int stagesCount, int signatureDimension, String superBitFileName, String indexField) {
		this.stagesCount = stagesCount;
		this.signatureDimension = signatureDimension;
		this.superBitFileName = superBitFileName;
		this.indexField = indexField;
	}
	
	@PostConstruct
	public void init() throws Exception {
		SuperBit superBit;
		
		File serializedSuperBitFile = new File(fileStorageService.getAbsolutePath(superBitFileName));
		if (serializedSuperBitFile.exists()) {
			try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedSuperBitFile));) {
				superBit = (SuperBit) objectInputStream.readObject();
			}
		} else {
			superBit = new SuperBit(signatureDimension);
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializedSuperBitFile))) {
				objectOutputStream.writeObject(superBit);
			} 
		}
		
		simHash = new SimHash(superBit, stagesCount);
	}

	@Override
	public long[] generateHashes(Resource resource) throws Exception {
		RealVector tfIdf = termsStatisticsService.getTfIdf(resource, indexField);
		return simHash.generateHashes(tfIdf);
	}

}
