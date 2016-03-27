package ru.mipt.pim.server.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

@Component
public class LanguageDetector {
	
	private Log logger = LogFactory.getLog(getClass());

	public String detectLang(String text) throws LangDetectException {
		Detector langDetector = DetectorFactory.create();
		langDetector.append(text);
		String lang = langDetector.detect();
		return lang;
	}

	public String detectLang(File contentFile) throws LangDetectException, IOException, FileNotFoundException {
		Detector langDetector = DetectorFactory.create();
		langDetector.append(new FileReader(contentFile));
		String lang = "en";
		try {
			lang = langDetector.detect();
		} catch (LangDetectException e) {
			logger.error("Error while detecting language", e);
		}
		return lang;
	}
	
}
