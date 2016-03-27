package ru.mipt.pim.server.repositories;

import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.MindMapNode;

@Service
public class MindMapNodeRepository extends CommonRepository<MindMapNode> {

	public MindMapNodeRepository() {
		super(MindMapNode.class);
	}

}
