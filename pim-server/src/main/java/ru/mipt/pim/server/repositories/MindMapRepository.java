package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.MindMap;

@Service
public class MindMapRepository extends CommonRepository<MindMap> {

	public MindMapRepository() {
		super(MindMap.class);
	}

}
