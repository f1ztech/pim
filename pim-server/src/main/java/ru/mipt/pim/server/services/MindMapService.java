package ru.mipt.pim.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.MindMap;
import ru.mipt.pim.server.model.MindMapNode;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.presentations.MindMup;
import ru.mipt.pim.server.presentations.MindMupNode;
import ru.mipt.pim.server.presentations.MindMupNode.MindMapNodeAttributes;
import ru.mipt.pim.server.presentations.MindMupNode.MindMapNodeIcon;
import ru.mipt.pim.server.repositories.MindMapNodeRepository;
import ru.mipt.pim.server.repositories.MindMapRepository;

@Component
public class MindMapService {

	@Autowired
	private UserService userService;

	@Autowired
	private MindMapRepository mindMapRepository;

	@Autowired
	private MindMapNodeRepository mindMapNodeRepository;

	@Autowired
	private ResourceComparator resourceComparator;

	public MindMap createMindMapFromResource(Resource resource) {
		MindMap mindMap = new MindMap();

		MindMapNode rootNode = createMindMapNode(resource, 1, true);
		mindMap.setRootNode(rootNode);
		mindMap.setOwner(userService.getCurrentUser());
		mindMap.setTitle(resource.getTitle());

		mindMapRepository.save(mindMap);
		return mindMap;
	}

	private MindMapNode createMindMapNode(Resource resource, int index, boolean useNegativeIndexes) {
		MindMapNode node = new MindMapNode();
		node.setRepresentedResource(resource);
		node.setTitle(resource.getTitle());
		node.setNodeIndex(index);

		resource.getNarrowerResources().sort(resourceComparator);
		int childIndex = 1, size = resource.getNarrowerResources().size();
		for (Resource child : resource.getNarrowerResources()) {
			if (useNegativeIndexes && size / 2 < childIndex) {
				childIndex = 0; // go to negative values after middle of the list
			}
			int nextIndex = childIndex <= 0 ? childIndex-- : childIndex++;

			node.getChildNodes().add(createMindMapNode(child, nextIndex, false));
		}
		mindMapNodeRepository.save(node);
		return node;
	}

	public MindMup buildMindMap(MindMap mindMap) {
		MindMup map = new MindMup();
		populateMindMupNode(map, map, mindMap.getRootNode());
		return map;
	}

	private MindMupNode populateMindMupNode(MindMup map, MindMupNode node, MindMapNode persistentNode) {
		node.setTitle(persistentNode.getTitle());
		int nextId = map.nextId();
		map.getIdToUriMap().put(nextId, persistentNode.getId());
		node.setId(nextId);

		MindMapNodeAttributes attr = new MindMapNodeAttributes();
		MindMapNodeIcon icon = new MindMapNodeIcon();
		icon.setIconClass(persistentNode.getRepresentedResource() != null ? persistentNode.getRepresentedResource().getIcon() : null);
		attr.setIcon(icon);
		node.setAttr(attr);

		persistentNode.getChildNodes().forEach(childNode -> {
			node.addIdea(populateMindMupNode(map, new MindMupNode(), childNode));
		});
		return node;
	}

}
