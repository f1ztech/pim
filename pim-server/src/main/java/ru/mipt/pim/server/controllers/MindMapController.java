package ru.mipt.pim.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.mipt.pim.server.model.MindMap;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.presentations.MindMup;
import ru.mipt.pim.server.repositories.MindMapNodeRepository;
import ru.mipt.pim.server.repositories.MindMapRepository;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.services.MindMapService;
import ru.mipt.pim.server.services.PermissionService;

@Controller
@RequestMapping("/mindmaps")
public class MindMapController {

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private MindMapRepository mindMapRepository;

	@Autowired
	private MindMapNodeRepository mindMapNodeRepository;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private MindMapService mindMapService;

	private void assertCanManage(Resource resource) {
		if (!permissionService.canManage(resource)) {
			throw new RuntimeException("Access denied");
		}
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	public @ResponseBody String create(@RequestBody String resourceId) {
		Resource resource = resourceRepository.findById(resourceId);
		assertCanManage(resource);

		MindMap mindMap = mindMapService.createMindMapFromResource(resource);
		return mindMap.getId();
	}

	@RequestMapping(value = "{mapId}", method = RequestMethod.GET)
	public @ResponseBody MindMup view(@PathVariable("mapId") String id) {
		MindMap mindMap = mindMapRepository.findById(id);
		assertCanManage(mindMap);

		return mindMapService.buildMindMap(mindMap);
	}

}
