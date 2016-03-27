package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Publication;


@Service
public class PublicationRepository extends CommonResourceRepository<Publication> {

	public PublicationRepository() {
		super(Publication.class);
	}

}
