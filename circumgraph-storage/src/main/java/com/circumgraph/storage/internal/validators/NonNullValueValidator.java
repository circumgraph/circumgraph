package com.circumgraph.storage.internal.validators;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.Value;

/**
 * Validator for dealing with anything that can not be null.
 */
public class NonNullValueValidator
	implements ValueValidator<Value>
{
	@Override
	public void validate(Value value, Consumer<ValidationMessage> validationCollector)
	{
		if(value == null)
		{
			validationCollector.accept(ValidationMessage.error()
				.withMessage("Value can not be null")
				.build()
			);
		}
	}
}
