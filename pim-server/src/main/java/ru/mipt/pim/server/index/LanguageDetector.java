package ru.mipt.pim.server.index;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

@Component
public class LanguageDetector {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@PostConstruct
	private void init() throws LangDetectException, URISyntaxException {
		DetectorFactory.loadProfile(new File(getClass().getClassLoader().getResource("META-INF/langdetect").toURI()));
	}

	public String detectLang(String text) {
		Detector langDetector = cerateDetector();
		langDetector.append(text);
		return detectLang(langDetector);
	}

	private String detectLang(Detector langDetector) {
		String lang = "en";
		try {
			lang = langDetector.detect();
		} catch (LangDetectException e) {
			if (!e.getMessage().contains("no features in text")) {
				logger.debug("Error while detecting language", e);
			}
		}
		return "ru".equals(lang) ? lang : "en";
	}

	public String detectLang(File contentFile) {
		Detector langDetector = cerateDetector();
		try {
			langDetector.append(new FileReader(contentFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return detectLang(langDetector);
	}

	private Detector cerateDetector() {
		try {
			return DetectorFactory.create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
