package ru.mipt.pim.server.similarity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.aduna.iteration.Iterations;
import ru.mipt.pim.server.model.ObjectWithRdfId;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

@Component("structure")
public class StructureHashingStrategy implements LshStrategy {

	public static int HASH_FUNCTIONS_COUNT = 500;
	public static int HASHES_COUNT = 15;
	
	@Autowired
	private Repository repository;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	private ValueFactory valueFactory;

	private MinHash minHash;
	
	private List<URI> ignoredPredicates = new ArrayList<URI>();
	
	@PostConstruct
	public void init() throws IOException, ClassNotFoundException {
		valueFactory = repository.getValueFactory();
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/owner"));
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/activationSubject"));
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/actionSubject"));
		ignoredPredicates.add(valueFactory.createURI("http://mipt.ru/pim/id"));
		ignoredPredicates.add(valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		
		File serializedMinHashFile = new File(fileStorageService.getAbsolutePath("structureMinHash"));
		if (serializedMinHashFile.exists()) {
			try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedMinHashFile));) {
				minHash = (MinHash) objectInputStream.readObject();
			}
		} else {
			minHash = new MinHash(HASH_FUNCTIONS_COUNT, HASHES_COUNT);
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializedMinHashFile))) {
				objectOutputStream.writeObject(minHash);
			} 
		}
	}
	
	@Override
	public long[] generateHashes(User user, ObjectWithRdfId resource) throws Exception {
		URI resourceUri = valueFactory.createURI(resource.getUri());
		RepositoryConnection connection = repository.getConnection();
		
		Set<Statement> siblings = new HashSet<>();
		siblings.addAll(Iterations.asList(connection.getStatements(resourceUri, null, null, true)));
		siblings.addAll(Iterations.asList(connection.getStatements(null, null, resourceUri, true)));
		
		return minHash.generateHashes(siblings.stream()
				.filter(statement -> !ignoredPredicates.contains(statement.getPredicate()))
				.map(statement -> statement.getObject().equals(resourceUri) ? statement.getSubject() : statement.getObject())
				.collect(Collectors.toSet()));
	}

}
