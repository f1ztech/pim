package ru.mipt.pim.server.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.Tag;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.FileRepository;
import ru.mipt.pim.server.repositories.UserRepository;
import ru.mipt.pim.server.services.RecommendationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/application-context.xml" })
public class RecommendationEfficiencyTest {

	public static class TestConfig {
		private int power;
		private boolean useTitles;
		private boolean useAbstracts;

		public TestConfig(int power, boolean useTitles, boolean useAbstracts) {
			this.power = power;
			this.useTitles = useTitles;
			this.useAbstracts = useAbstracts;
		}

		public int getPower() {
			return power;
		}

		public void setPower(int power) {
			this.power = power;
		}

		public boolean isUseTitles() {
			return useTitles;
		}

		public void setUseTitles(boolean useTitles) {
			this.useTitles = useTitles;
		}

		public boolean isUseAbstracts() {
			return useAbstracts;
		}

		public void setUseAbstracts(boolean useAbstracts) {
			this.useAbstracts = useAbstracts;
		}

		@Override
		public String toString() {
			return "power" + power + " useTitles: " + useTitles + "; useAbstracts: " + useAbstracts;
		}
	}

	public static class TestResult implements Comparable<TestResult> {
		private TestConfig config;
		private Map<Integer, Integer> result;

		public TestResult(TestConfig config, Map<Integer, Integer> result) {
			this.config = config;
			this.result = result;
		}

		public TestConfig getConfig() {
			return config;
		}

		public void setConfig(TestConfig config) {
			this.config = config;
		}

		@Override
		public int compareTo(TestResult results2) {
			return Float.compare(getScore(), results2.getScore());
		}
		
		public float getScore() {
			return (float) getResult().entrySet().stream().collect(Collectors.summarizingDouble(entry -> entry.getKey() *  entry.getValue())).getSum()
					/ (float) getResult().entrySet().stream().collect(Collectors.summarizingDouble(entry -> entry.getValue())).getSum();
		}

		public Map<Integer, Integer> getResult() {
			return result;
		}

		public void setResult(Map<Integer, Integer> result) {
			this.result = result;
		}
		
		@Override
		public String toString() {
			return config.toString() + "\n" + result.toString();
		}
	}

	private List<TestResult> results = new ArrayList<>();
	
	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testRecommendations() throws IOException {
		User user = userRepository.findByLogin("andreyb");
		List<File> files = fileRepository.findAll(user);
		for (int power = 2; power <= 13; power++) {
			if (power == 3) power=4;
			if (power == 5) power=7;
			testEfficiency(new TestConfig(power, false, false), user, files);
			testEfficiency(new TestConfig(power, true, false), user, files);
			testEfficiency(new TestConfig(power, true, true), user, files);
		}
		
		Collections.sort(results);
		System.out.println(results);
	}

	private void configureRecommendationService(TestConfig config) {
		RecommendationService.power = config.getPower();
		RecommendationService.useTitles = config.isUseTitles();
		RecommendationService.useAbstracts = config.isUseAbstracts();
	}

	private TestResult testEfficiency(TestConfig config, User user, List<File> files) throws IOException {
		configureRecommendationService(config);
		
		Map<Integer, Integer> placesMap = new HashMap<Integer, Integer>();
		for (File file : files) {
			if (file.getBroaderResources().isEmpty()) {
				continue;
			}
			Resource folder = file.getBroaderResources().get(0);
			if (folder.getNarrowerResources().size() < 3) {
				continue;
			}

			List<Tag> recommendations = recommendationService.getRecommendations(user, file);
			if (recommendations.isEmpty()) {
				continue;
			}
			int correctTagIndex = 0;
			for (Tag recommendation : recommendations) {
				correctTagIndex++;
				if (isEqualsOrNarrower(recommendation, folder)) {
					break;
				}
			}
			if (placesMap.get(correctTagIndex) == null) {
				placesMap.put(correctTagIndex, 0);
			}

			placesMap.put(correctTagIndex, placesMap.get(correctTagIndex) + 1);
		}
		
		TestResult result = new TestResult(config, placesMap);
		
		System.out.println(result);
		results.add(result);
		
		return result;
	}

	private boolean isEqualsOrNarrower(Resource source, Resource target) {
		if (source.equals(target)) {
			return true;
		}

		for (Resource broader : source.getBroaderResources()) {
			if (isEqualsOrNarrower(broader, target)) {
				return true;
			}
		}
		return false;
	}

}
