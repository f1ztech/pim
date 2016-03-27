package ru.mipt.pim.server.model;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;


public enum ResourceType {
	TAG(Tag.class), NOTE(Note.class), MESSAGE(Message.class), EMAIL(Email.class), EMAIL_FOLDER(EmailFolder.class),
	PROJECT(Project.class), BOOKMARK(Bookmark.class), EVENT(Event.class), PERSON(Person.class), MINDMAP(MindMap.class), PUBLICATION(Publication.class);

	private static BidiMap<ResourceType, Class<? extends Resource>> resourceTypeClassMap;

	private ResourceType(Class<? extends Resource> clazz) {
		this.clazz = clazz;
	}

	private Class<? extends Resource> clazz;

	public Class<? extends Resource> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends Resource> clazz) {
		this.clazz = clazz;
	}

	public static ResourceType findByClass(Class<? extends Resource> clazz) {
		if (resourceTypeClassMap == null) {
			resourceTypeClassMap = new DualHashBidiMap<>();
			for (ResourceType type : ResourceType.values()) {
				resourceTypeClassMap.put(type, type.getClazz());
			}
		}
		return resourceTypeClassMap.getKey(clazz);
	}
}