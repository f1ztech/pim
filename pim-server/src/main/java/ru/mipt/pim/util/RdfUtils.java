package ru.mipt.pim.util;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.springframework.util.Assert;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.util.BeanReflectUtil;
import com.complexible.common.base.Dates;

import ru.mipt.pim.server.model.ObjectWithRdfId;

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
	
	public static Map<String, String> getNamespacesMap(Class<?> clazz) {
		Map<String, String> ret = new HashMap<>();
		String[] namespacesArr = getNamespaces(clazz);
		for (int i = 0; i < namespacesArr.length - 1; i += 2) {
			ret.put(namespacesArr[i], namespacesArr[i + 1]);
		}
		return ret;
	}

	public static String getRdfProperty(Class<?> objectClazz, String propertyName) {
		RdfProperty rdfPropertyAnnotation;
		try {
			rdfPropertyAnnotation = objectClazz.getDeclaredField(propertyName).getAnnotation(RdfProperty.class);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}

		String property = rdfPropertyAnnotation.value();
		return property;
	}

	static final List<URI> integerTypes = Arrays.asList(XMLSchema.INT, XMLSchema.INTEGER, XMLSchema.POSITIVE_INTEGER,
													  XMLSchema.NEGATIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER,
													  XMLSchema.NON_POSITIVE_INTEGER, XMLSchema.UNSIGNED_INT);
	static final List<URI> longTypes = Arrays.asList(XMLSchema.LONG, XMLSchema.UNSIGNED_LONG);
	static final List<URI> floatTypes = Arrays.asList(XMLSchema.FLOAT, XMLSchema.DECIMAL);
	static final List<URI> shortTypes = Arrays.asList(XMLSchema.SHORT, XMLSchema.UNSIGNED_SHORT);
	static final List<URI> byteTypes = Arrays.asList(XMLSchema.BYTE, XMLSchema.UNSIGNED_BYTE);

	
	public static Object literalToObject(Literal literal) {
		URI type = literal.getDatatype() != null ? literal.getDatatype() : null;
		
		if (type == null || XMLSchema.STRING.equals(type) || RDFS.LITERAL.equals(type)) {
			return literal.getLabel();
		} else if (XMLSchema.BOOLEAN.equals(type)) {
			return Boolean.valueOf(literal.getLabel());
		} else if (integerTypes.contains(type)) {
			return Integer.parseInt(literal.getLabel());
		} else if (longTypes.contains(type)) {
			return Long.parseLong(literal.getLabel());
		} else if (XMLSchema.DOUBLE.equals(type)) {
			return Double.valueOf(literal.getLabel());
		} else if (floatTypes.contains(type)) {
			return Float.valueOf(literal.getLabel());
		} else if (shortTypes.contains(type)) {
			return Short.valueOf(literal.getLabel());
		} else if (byteTypes.contains(type)) {
			return Byte.valueOf(literal.getLabel());
		} else if (XMLSchema.ANYURI.equals(type)) {
			return literal.getLabel();
		} else if (XMLSchema.DATE.equals(type) || XMLSchema.DATETIME.equals(type)) {
			return Dates.asDate(literal.getLabel());
		} else if (XMLSchema.TIME.equals(type)) {
			return new Date(Long.parseLong(literal.getLabel()));
		}
		
		return literal.stringValue();
	}
}
