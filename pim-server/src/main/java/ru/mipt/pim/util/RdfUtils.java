package ru.mipt.pim.util;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

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
			String[] namespaces = getNamespaces(clazz);
			int namespaceIndex = ArrayUtils.indexOf(namespaces, namespace);
			Assert.isTrue(namespaceIndex >= 0, "namespace " + namespace + " not found for class " + clazz.getName());
			uri = namespaces[namespaceIndex + 1] + propertyName;
		}
		return uri;
	}

	private static String[] getNamespaces(Class<?> clazz) {
		String[] ret = new String[] {};
		
		Namespaces annotation = clazz.getAnnotation(Namespaces.class);
		if (annotation != null) {
			ret = ArrayUtils.addAll(ret, annotation.value());
		}
		
		if (clazz.getSuperclass() != null) {
			ret = ArrayUtils.addAll(ret, getNamespaces(clazz.getSuperclass()));	
		}
		
		return ret;
	}

}
