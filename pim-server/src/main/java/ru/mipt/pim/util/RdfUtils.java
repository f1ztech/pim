package ru.mipt.pim.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;

public class RdfUtils {

	public static String getRdfUri(Class<?> clazz) {
		RdfsClass rdfsClass = clazz.getDeclaredAnnotation(RdfsClass.class);
		if (rdfsClass != null) {
			return expandNamespace(clazz, rdfsClass.value());			
		} else {
			throw new RuntimeException("Cannot get rdf id namespace!");
		}
	}

	public static String expandNamespace(Class<?> clazz, String uri) {
		String namespace = StringUtils.substringBefore(uri, ":");
		String propertyName = StringUtils.substringAfter(uri, ":");
		if (!namespace.isEmpty()) {
			String[] namespaces = clazz.getAnnotation(Namespaces.class).value();
			int namespaceIndex = ArrayUtils.indexOf(namespaces, namespace);
			uri = namespaces[namespaceIndex + 1] + propertyName;
		}
		return uri;
	}

}
