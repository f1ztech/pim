package ru.mipt.pim.server.model;

import java.util.Date;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Entity
@Namespaces({
	"foaf", "http://xmlns.com/foaf/0.1/",
	"dc",   "http://purl.org/dc/terms/",
	"pim",  "http://mipt.ru/pim/",
	"skos", "http://www.w3.org/2004/02/skos/core#",
	"nfo",  "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",
	"pimo", "http://www.semanticdesktop.org/ontologies/2007/11/01/pimo/#",
	"ncal", "http://www.semanticdesktop.org/ontologies/2007/04/02/ncal/#"
})
@RdfsClass("pim:UserAction")
public class UserAction extends ObjectWithRdfId {

	public static enum UserActionType {
		VIEW;
	}
	
	@RdfProperty("pim:actionDate")
	private Date actionDate;
	
	@RdfProperty("pim:actionSubject")
	private Resource actionSubject;
	
	private UserActionType actionType;

	public UserActionType getActionType() {
		return actionType;
	}

	public void setActionType(UserActionType actionType) {
		this.actionType = actionType;
	}

	public Resource getActionSubject() {
		return actionSubject;
	}

	public void setActionSubject(Resource actionSubject) {
		this.actionSubject = actionSubject;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	public String getActionTypeString() {
		return actionType == null ? null : actionType.name();
	}

	@RdfProperty("pim:actionType")
	public void setActionTypeString(String actionTypeString) {
		this.actionType = actionTypeString == null ? null : UserActionType.valueOf(actionTypeString);
	}
	
}
