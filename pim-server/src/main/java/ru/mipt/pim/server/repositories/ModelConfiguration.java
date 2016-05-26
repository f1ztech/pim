package ru.mipt.pim.server.repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.complexible.common.util.PrefixMapping;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.util.RdfUtils;

@Component
public class ModelConfiguration {
	
	public static Map<String, Class<? extends Resource>> CLASS_URI_MAP = new HashMap<>();

	@PostConstruct
	public void init() throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Namespaces.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("ru.mipt.pim.server.model")) {
			for (Entry<String, String> namespace : RdfUtils.getNamespacesMap(Class.forName(bd.getBeanClassName())).entrySet()) {
				PrefixMapping.GLOBAL.addMapping(namespace.getKey(), namespace.getValue());	
			}
		}
		
		scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(RdfsClass.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("ru.mipt.pim.server.model")) {
			Class<? extends Resource> clazz = (Class<? extends Resource>) Class.forName(bd.getBeanClassName());
			CLASS_URI_MAP.put(clazz.getAnnotation(RdfsClass.class).value(), clazz);
		}
	}
	
}
