package ru.mipt.pim.server.model;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Entity
@Namespaces({
	"pim",  "http://mipt.ru/pim/"
})
@RdfsClass("pim:Role")
public class Role extends ObjectWithRdfId {
	
	@OneToOne
	@RdfProperty("pim:roleUser")
	private User user;
	
	@RdfProperty("pim:roleValue")
	private Integer role;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getRole() {
		return role;
	}
	public void setRole(Integer role) {
		this.role = role;
	}
}
