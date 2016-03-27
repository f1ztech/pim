package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Person;

@Service
public class PersonRepository extends CommonResourceRepository<Person> {

	public PersonRepository() {
		super(Person.class);
	}

}
