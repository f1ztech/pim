package ru.mipt.pim.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.complexible.common.base.Dates;
import com.complexible.common.util.PrefixMapping;

public class RdfUtils {
	
	private static final ValueFactory FACTORY = new ValueFactoryImpl();

	public static String getRdfUri(Class<?> clazz) {
		RdfsClass rdfsClass = clazz.getDeclaredAnnotation(RdfsClass.class);
		if (rdfsClass != null) {
			return expandNamespace(rdfsClass.value());			
		} else {
			throw new RuntimeException("Cannot get rdf id namespace!");
		}
	}

	public static String expandNamespace(String uri) {
		String namespacePrefix = StringUtils.substringBefore(uri, ":");
		String propertyName = StringUtils.substringAfter(uri, ":");
		
		return PrefixMapping.GLOBAL.getNamespace(namespacePrefix) + propertyName;
	}

	public static String collapseNamespace(String uri) {
		for (String prefix : PrefixMapping.GLOBAL.getPrefixes()) {
			String namespace = PrefixMapping.GLOBAL.getNamespace(prefix);
			if (uri.contains(namespace)) {
				return uri.replace(namespace, prefix + ":");
			}
		}
		throw new RuntimeException("Prefix for namespace of uri " + uri + " not found");
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
			rdfPropertyAnnotation = getDeclaredField(objectClazz, propertyName).getAnnotation(RdfProperty.class);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}

		String property = rdfPropertyAnnotation.value();
		return property;
	}

	private static Field getDeclaredField(Class<?> objectClazz, String propertyName) throws NoSuchFieldException {
		try {
			return objectClazz.getDeclaredField(propertyName);
		} catch (NoSuchFieldException e) {
			if (objectClazz.getSuperclass() != null) {
				return getDeclaredField(objectClazz.getSuperclass(), propertyName);
			}
			throw e;
		}
	}

	static final List<URI> integerTypes = Arrays.asList(XMLSchema.INT, XMLSchema.INTEGER, XMLSchema.POSITIVE_INTEGER,
													  XMLSchema.NEGATIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER,
													  XMLSchema.NON_POSITIVE_INTEGER, XMLSchema.UNSIGNED_INT);
	static final List<URI> longTypes = Arrays.asList(XMLSchema.LONG, XMLSchema.UNSIGNED_LONG);
	static final List<URI> floatTypes = Arrays.asList(XMLSchema.FLOAT, XMLSchema.DECIMAL);
	static final List<URI> shortTypes = Arrays.asList(XMLSchema.SHORT, XMLSchema.UNSIGNED_SHORT);
	static final List<URI> byteTypes = Arrays.asList(XMLSchema.BYTE, XMLSchema.UNSIGNED_BYTE);

	
	public static Object literalToObject(Literal literal) {
		if (literal == null) {
			return null;
		}
		
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
	
	public static Value objectToValue(Object object) {
		if (object == null) {
			return null;
		} else if (Boolean.class.isInstance(object)) {
			return FACTORY.createLiteral(Boolean.class.cast(object).booleanValue());
		} else if (Integer.class.isInstance(object)) {
			return FACTORY.createLiteral(Integer.class.cast(object).intValue());
		} else if (Long.class.isInstance(object)) {
			return FACTORY.createLiteral(Long.class.cast(object).longValue());
		} else if (Short.class.isInstance(object)) {
			return FACTORY.createLiteral(Short.class.cast(object).shortValue());
		} else if (Double.class.isInstance(object)) {
			return FACTORY.createLiteral(Double.class.cast(object).doubleValue());
		} else if (Float.class.isInstance(object)) {
			return FACTORY.createLiteral(Float.class.cast(object).floatValue());
		} else if (Date.class.isInstance(object)) {
			return FACTORY.createLiteral(Dates.datetime(Date.class.cast(object)), XMLSchema.DATETIME);
		} else if (String.class.isInstance(object)) {
			return FACTORY.createLiteral(String.class.cast(object), XMLSchema.STRING);
		} else if (Character.class.isInstance(object)) {
			return FACTORY.createLiteral(Character.class.cast(object));
		} else if (java.net.URI.class.isInstance(object)) {
			return FACTORY.createURI(object.toString());
		} else if (Value.class.isAssignableFrom(object.getClass())) {
			return Value.class.cast(object);
		}
		
		throw new RuntimeException(object.getClass().getName() + " cannot be converted to Value");
	}
}
