package ru.mipt.pim.server.repositories;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.User;


@Service
public class UserRepository extends CommonResourceRepository<User> {

	public UserRepository() {
		super(User.class);
	}

	public User findByLogin(String login) {
		Query query = prepareQuery("where { ?result foaf:nick ??login }");
		query.setParameter("login", login);

		return getFirst(query);
	}

	public List<User> findByNotNullOauthEmailUser() {
		Query query = prepareQuery("where { "
				+ " 	?result <http://mipt.ru/pim/userConfigs> ?userConfigs. "
				+ " 	?userConfigs <http://mipt.ru/pim/oauthEmailUser> ?oauthUser"
				+ " }");

		return getResultList(query);
	}
	
	@Override
	protected String getLanguageString(User resource) {
		return StringUtils.defaultIfBlank(super.getLanguageString(resource), resource.getLogin());
	}

}
