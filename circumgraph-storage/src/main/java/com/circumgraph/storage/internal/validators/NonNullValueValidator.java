package com.circumgraph.storage.internal.validators;

import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.Value;

/**
 * Validator for dealing with anything that can not be null.
 */
public class NonNullValueValidator
	implements ValueValidator<Value>
{
	@Override
	public void validate(Value value, ValidationMessageCollector collector)
	{
		if(value == null)
		{
			collector.error()
				.withMessage("Value can not be null")
				.done();
		}
	}
}
