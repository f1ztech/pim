package ru.mipt.pim.server.controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.ResourceType;
import ru.mipt.pim.server.model.Tag;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.model.UserAction;
import ru.mipt.pim.server.model.UserAction.UserActionType;
import ru.mipt.pim.server.recommendations.RecommendationService;
import ru.mipt.pim.server.repositories.ContactRepository;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.repositories.TagRepository;
import ru.mipt.pim.server.repositories.UserActionRepository;
import ru.mipt.pim.server.services.ActivationService;
import ru.mipt.pim.server.services.PermissionService;
import ru.mipt.pim.server.services.ResourceComparator;
import ru.mipt.pim.server.services.UserService;

@Controller
@RequestMapping(value = "/workbench")
public class WorkbenchController {

	public static class UpdateResourceBody {
		private String uri;
		private String property;
		private Object value;

		public String getProperty() {
			return property;
		}
		public void setProperty(String property) {
			this.property = property;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public String getUri() {
			return uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
	}

	public static class NewResourceBody {
		private ResourceType resourceType;
		private String broaderResource;

		public ResourceType getResourceType() {
			return resourceType;
		}

		public void setResourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
		}

		public String getBroaderResource() {
			return broaderResource;
		}

		public void setBroaderResource(String broaderResource) {
			this.broaderResource = broaderResource;
		}
	}

	public static class BroaderResourcesResponse {
		private List<String> path;
		private List<Resource> data;

		public BroaderResourcesResponse(List<String> path, List<Resource> data) {
			this.path = path;
			this.data = data;
		}

		public List<String> getPath() {
			return path;
		}
		public void setPath(List<String> path) {
			this.path = path;
		}
		public List<Resource> getData() {
			return data;
		}
		public void setData(List<Resource> data) {
			this.data = data;
		}
	}

	public static class ChangeParentBody {
		private String sourceResource;
		private String newParent;
		public String getSourceResource() {
			return sourceResource;
		}
		public void setSourceResource(String sourceResource) {
			this.sourceResource = sourceResource;
		}
		public String getNewParent() {
			return newParent;
		}
		public void setNewParent(String newParent) {
			this.newParent = newParent;
		}
	}

	public static class AddTagBody {
		private String resourceId;
		private String tagName;
		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}
		public String getTagName() {
			return tagName;
		}
		public void setTagName(String tagName) {
			this.tagName = tagName;
		}
	}

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private UserActionRepository userActionRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private ActivationService activationService;

	@Autowired
	private UserService userService;

	@Autowired
	private Repository repository;

	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private ResourceComparator resourceComparator;

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private IndexingService indexingService;

	private ValueFactory valueFactory;
	private URI pNarrower;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private URI pRelated;

	private URI pBroader;


	@PostConstruct
	public void init() {
		valueFactory = repository.getValueFactory();
		pNarrower = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#narrower");
		pBroader = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#broader");
		pRelated = valueFactory.createURI("http://www.w3.org/2004/02/skos/core#related");
	}

	@RequestMapping(value = "resources", method = RequestMethod.GET)
	public @ResponseBody List<Resource> findRoots() {
		List<Resource> rootResources = resourceRepository.findRootResources(userService.getCurrentUser());
		rootResources.iterator().next().getId();
		rootResources.forEach(r -> resourceComparator.isHasNarrowerResources(r));
		rootResources.sort(resourceComparator);
		return rootResources;
	}

	@RequestMapping(value = "narrowerResources", method = RequestMethod.GET)
	public @ResponseBody List<Resource> findNarrowerResources(@RequestParam("uri") String uri) {
		Resource resource = resourceRepository.find(uri);
		assertCanManage(resource);

		List<Resource> ret = resource != null ? resource.getNarrowerResources() : Collections.emptyList();
		ret.forEach(r -> resourceComparator.isHasNarrowerResources(r));
		ret.sort(resourceComparator);
		return ret;
	}

	@RequestMapping(value = "broaderResources", method = RequestMethod.GET)
	public @ResponseBody BroaderResourcesResponse findBroaderResources(@RequestParam("id") String resourceId) {
		Resource resource = resourceRepository.findById(resourceId);
		assertCanManage(resource);

		List<Resource> path = new ArrayList<>();
		resourceComparator.isHasNarrowerResources(resource);
		path.add(resource);
		Resource currentResource = resource;
		while (!currentResource.getBroaderResources().isEmpty()) {
			currentResource = currentResource.getBroaderResources().get(0);
			path.add(0, currentResource);
		}

		List<Resource> rootResources = resourceRepository.findRootResources(userService.getCurrentUser());
		rootResources.sort(resourceComparator);
		List<Resource> narrowerResources = rootResources;
		for (Resource pathResource : path) {
			Resource narrowerResource = narrowerResources.stream()
					.filter(res -> res.getUri().equals(pathResource.getUri()))
					.findFirst().orElseThrow(() -> new RuntimeException("resource not found in user repository"));
			narrowerResource.setIncludeNarrowerResourcesToJson(true);

			narrowerResources = narrowerResource.getNarrowerResources();
			narrowerResources.sort(resourceComparator);
		}

		return new BroaderResourcesResponse(path.stream().map(Resource::getId).collect(Collectors.toList()), rootResources);
	}

	@RequestMapping(value = "resources/update", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.OK)
	public void updateResource(@RequestBody UpdateResourceBody updateBody) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, RepositoryException, IOException, LangDetectException {
		Resource resource = resourceRepository.find(updateBody.getUri());
		assertCanManage(resource);

		if (!updateBody.getProperty().equals("tags")) {
			PropertyUtils.setProperty(resource, updateBody.getProperty(), updateBody.getValue());
		} else {
			RepositoryConnection connection = repository.getConnection();
			List<Tag> newTags = ((List<Map<String, Object>>) updateBody.getValue()).stream()
					.map(map -> tagRepository.findById((String) map.get("id")))
					.filter(tag -> tag != null).collect(Collectors.toList());
			List<Tag> existingTags = resource.getTags();
			for (Tag tag : existingTags) {
				if (!newTags.contains(tag)) {
					URI tagUri = valueFactory.createURI(tag.getUri());
					connection.remove(valueFactory.createURI(resource.getUri()), pRelated, tagUri);
					connection.remove(valueFactory.createURI(resource.getUri()), pBroader, tagUri);
					if (!connection.hasStatement(null, pRelated, tagUri, true)) {
						connection.remove(tagUri, null, null);
						connection.remove(null, null, (Value) tagUri);
					}
				}
			}
			for (Tag tag : newTags) {
				if (!existingTags.contains(tag)) {
					connection.add(valueFactory.createURI(resource.getUri()), pRelated, valueFactory.createURI(tag.getUri()));
				}
			}
			
			indexingService.setTags(userService.getCurrentUser(), resource.getId(), newTags.stream().map(Tag::getId).collect(Collectors.toList()));
			
			connection.commit();
		}
	}


	private void assertCanManage(Resource resource) {
		if (!permissionService.canManage(resource)) {
			throw new RuntimeException("Access denied");
		}
	}

	@RequestMapping(value = "resources", method = RequestMethod.POST)
	public @ResponseBody Resource newResource(@RequestBody NewResourceBody newResourceBody) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Resource broaderResource = null;
		if (newResourceBody.getBroaderResource() != null) {
			broaderResource = resourceRepository.findById(newResourceBody.getBroaderResource());
			assertCanManage(broaderResource);
		}

		Class<? extends Resource> resourceClass = newResourceBody.getResourceType().getClazz();
		Resource resource = resourceClass.newInstance();
		resource.setOwner(userService.getCurrentUser());
		resource.setTitle("(без названия)");
		resourceRepository.save(resource);

		if (broaderResource != null) {
			broaderResource.getNarrowerResources().add(resource);
			resourceRepository.merge(broaderResource);
		}

		return resource;
	}

	@RequestMapping(value = "changeParent", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void changeParent(@RequestBody ChangeParentBody body) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Resource sourceResource = resourceRepository.findById(body.getSourceResource());
		assertCanManage(sourceResource);
		Resource newParent = resourceRepository.findById(body.getNewParent());
		assertCanManage(newParent);

		sourceResource.getBroaderResources().clear();
		newParent.getNarrowerResources().add(sourceResource);
		resourceRepository.merge(newParent);
	}

	@RequestMapping(value = "resources/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void delete(@PathVariable("id") String id) {
		Resource sourceResource = resourceRepository.findById(id);
		assertCanManage(sourceResource);

		removeNarrowerResources(sourceResource);
	}

	private void removeNarrowerResources(Resource resource) {
		resource.getBroaderResources().clear();
		resource.getNarrowerResources().forEach(this::removeNarrowerResources);
		resourceRepository.remove(resource);
	}

	@RequestMapping("logAction")
	public @ResponseBody List<Resource> logAction(@RequestParam("id") String id, @RequestParam("actionType") String actionType) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
		Resource resource = resourceRepository.findById(id);
		assertCanManage(resource);

		UserAction action = new UserAction();
		action.setActionSubject(resource);
		action.setActionDate(new Date());
		action.setActionType(UserActionType.valueOf(actionType));
		userActionRepository.save(action);

		User user = userService.getCurrentUser();
		activationService.spreadActivation(user, resource);

		return activationService.getTopActivatedResources(user);
	}

	@RequestMapping("activatedResources")
	public @ResponseBody List<Resource> getAtctivatedResources() {
		return activationService.getTopActivatedResources(userService.getCurrentUser());
	}

	@RequestMapping("recommendations")
	public @ResponseBody List<Tag> getRecommendations(@RequestParam String id) throws IOException {
		Resource resource = resourceRepository.findById(id);
		assertCanManage(resource);

		return recommendationService.getRecommendations(userService.getCurrentUser(), resource);
	}

	@RequestMapping(value = "tags", method=RequestMethod.GET)
	public @ResponseBody List<Tag> findTags(@RequestParam(value = "query", required = false) String query) throws IOException, URISyntaxException {
		return tagRepository.findByTitleLike(userService.getCurrentUser(), query);
	}

	@RequestMapping(value = "tags", method=RequestMethod.POST)
	public @ResponseBody Tag addTag(@RequestBody AddTagBody body) {
		if (body.getTagName().isEmpty()) {
			return null;
		}

 		Resource resource = resourceRepository.findById(body.getResourceId());
		assertCanManage(resource);

		User currentUser = userService.getCurrentUser();
		Tag tag = tagRepository.findFirstByTitle(currentUser, body.getTagName());
		if (tag == null) {
			tag = new Tag();
			tag.setTitle(body.getTagName());
			tag.setOwner(currentUser);
			tagRepository.save(tag);
		}
		return tag;
	}
}
