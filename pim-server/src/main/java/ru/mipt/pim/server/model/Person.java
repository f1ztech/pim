package ru.mipt.pim.server.model;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

import ru.mipt.pim.server.index.Indexable;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/", 
	"pim",  "http://mipt.ru/pim/"
})
@RdfsClass("foaf:Person")
public class Person extends Resource implements Indexable {

	@RdfProperty("foaf:firstName")
	private String firstName;
	
	@RdfProperty("foaf:surname")
	private String lastName;
	
	@RdfProperty("foaf:middleName") // TODO exists?
	private String middlename;
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}
	
}
