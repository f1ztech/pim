package ru.mipt.pim.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.repositories.ContactRepository;
import ru.mipt.pim.server.repositories.ResourceRepository;
import ru.mipt.pim.server.services.UserService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private UserService userService;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		return "home";
	}

	@RequestMapping(value = "/dashboard")
	public String dashboard() {
		return "dashboard";
	}

	@RequestMapping(value = "/pr")
	public String projects() {
		return "pr";
	}

	@RequestMapping(value="/search")
	public @ResponseBody List<Resource> search(@RequestParam String query) throws IOException, LangDetectException, ParseException {
		return resourceRepository.findByFulltext(userService.getCurrentUser(), query);
	}
}
