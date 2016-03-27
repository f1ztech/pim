package ru.mipt.pim.server.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"pim",  "http://mipt.ru/pim/"
})
@RdfsClass("pim:User")
public class User extends Person {

	@NotNull
	@Size(min=3)
	@RdfProperty("foaf:nick")
	private String login;

	@NotNull
	@Size(min=3)
	@RdfProperty("pim:password")
	private String password;

	@OneToOne(mappedBy="user", cascade={CascadeType.ALL})
	@RdfProperty("pim:role")
	private Role role;

	@OneToOne(fetch = FetchType.LAZY)
	@RdfProperty("pim:userConfigs")
	private UserConfigs userConfigs;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public UserConfigs getUserConfigs() {
		return userConfigs;
	}

	public void setUserConfigs(UserConfigs userConfigs) {
		this.userConfigs = userConfigs;
	}

}
