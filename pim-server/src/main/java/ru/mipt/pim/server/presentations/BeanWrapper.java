package ru.mipt.pim.server.presentations;

import java.util.HashMap;

import org.apache.commons.beanutils.PropertyUtils;

public class BeanWrapper<T> extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	private T wrappedObject;
	
	public BeanWrapper() {
	}
	
	public BeanWrapper(T wrappedObject, String... properties) {
		this.wrappedObject = wrappedObject;
		put(properties);
	}
	
	public void put(String... properties) {
		for (String property : properties) {
			try {
				put(property, PropertyUtils.getProperty(wrappedObject, property));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
