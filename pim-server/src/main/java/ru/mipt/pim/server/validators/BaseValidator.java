package ru.mipt.pim.server.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class BaseValidator implements Validator {
	
	private Validator baseValidator;

	public Validator getBaseValidator() {
		return baseValidator;
	}

	public void setBaseValidator(Validator baseValidator) {
		this.baseValidator = baseValidator;
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if(baseValidator != null) {
			baseValidator.validate(target, errors);
		}
	}

}
