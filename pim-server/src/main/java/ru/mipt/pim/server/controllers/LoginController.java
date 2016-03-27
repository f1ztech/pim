package ru.mipt.pim.server.controllers;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.mipt.pim.adapters.fs.common.JsonRequestResults.AuthenticateResult;
import ru.mipt.pim.server.rest.TokenUtils;

@Controller
@RequestMapping
public class LoginController {

	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;
	
	@Resource
	private UserDetailsService userDetailsService;

	@RequestMapping("/login/failure")
	public String failure() {
		return "login/failure";
	}

	@RequestMapping("/login/test")
	public String test() {
		return "login/test";
	}

	@RequestMapping("/rest/authenticate")
	public @ResponseBody AuthenticateResult authenticate(@RequestParam("login") String login, @RequestParam("password") String password) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login, password);
			Authentication authentication = authenticationManager.authenticate(authenticationToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
	
			/*
			 * Reload user as password of authentication principal will be null
			 * after authorization and password is needed for token generation
			 */
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(login);
	
			return new AuthenticateResult(TokenUtils.createToken(userDetails));
		} catch (Exception e) {
			return new AuthenticateResult(false);
		}
	}

}
