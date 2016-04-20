package ru.mipt.pim.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;

import ru.mipt.pim.server.model.Bookmark;
import ru.mipt.pim.server.model.Contact;
import ru.mipt.pim.server.model.ContactGroup;
import ru.mipt.pim.server.model.EmailFolder;
import ru.mipt.pim.server.model.Event;
import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Message;
import ru.mipt.pim.server.model.MindMap;
import ru.mipt.pim.server.model.Note;
import ru.mipt.pim.server.model.ObjectWithRdfId;
import ru.mipt.pim.server.model.Person;
import ru.mipt.pim.server.model.Project;
import ru.mipt.pim.server.model.Publication;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.Tag;

public class Utilities {

	public static <T> T getElement(List<T> list, int index) {
		return list.size() > index + 1 ? null : list.get(index);
	}

	public static void copyObject(Object to, Object from, String... except) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, Object> properties = PropertyUtils.describe(from);
		for(String property : properties.keySet()) {
			if(ArrayUtils.contains(except, property)) continue;
			if(PropertyUtils.getPropertyDescriptor(to, property).getWriteMethod() != null)
				PropertyUtils.setSimpleProperty(to, property, PropertyUtils.getSimpleProperty(from, property));
		}
	}

	public static String getIcon(Class<? extends Resource> clazz) {
		if (Folder.class.isAssignableFrom(clazz)) {
			return "glyphicon glyphicon-folder-close";
		} else if (File.class.isAssignableFrom(clazz)) {
			return "glyphicon glyphicon-file";
		} else if (Tag.class.isAssignableFrom(clazz)) {
			return "glyphicon glyphicon-tag";
		} else if (Publication.class.isAssignableFrom(clazz)) {
			return "fa fa-graduation-cap";
		} else if (Note.class.isAssignableFrom(clazz)) {
			return "fa fa-file-text-o";
		} else if (Message.class.isAssignableFrom(clazz)) {
			return "fa fa-envelope-o";
		} else if (EmailFolder.class.isAssignableFrom(clazz)) {
			return "fa fa-envelope-square";
		} else if (Event.class.isAssignableFrom(clazz)) {
			return "fa fa-calendar-o";
		} else if (Person.class.isAssignableFrom(clazz)) {
			return "fa fa-user";
		} else if (Contact.class.isAssignableFrom(clazz)) {
			return "fa fa-user";			
		} else if (ContactGroup.class.isAssignableFrom(clazz)) {
			return "fa fa-users";			
		} else if (Project.class.isAssignableFrom(clazz)) {
			return "fa fa-rocket";
		} else if (MindMap.class.isAssignableFrom(clazz)) {
			return "fa fa-sitemap";
		} else if (Bookmark.class.isAssignableFrom(clazz)) {
			return "fa fa-bookmark";
		}
		return null;
	}

}
