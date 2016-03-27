package ru.mipt.pim.server.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.UserRepository;
import ru.mipt.pim.server.validators.UserValidator;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@RequestMapping(value = "register", method = RequestMethod.GET)
	public void register(Model model) {
		model.addAttribute("user", new User());
	}

	@RequestMapping(value = "register", method = RequestMethod.POST)
	public String saveRegister(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "/user/register";
		}

		user.setPassword(encoder.encode(user.getPassword()));
		userRepository.save(user);

		return "redirect:/";
	}
}
