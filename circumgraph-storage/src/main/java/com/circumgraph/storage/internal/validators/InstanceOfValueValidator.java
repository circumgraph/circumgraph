package com.circumgraph.storage.internal.validators;

import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.SimpleValue;

/**
 * Validator for making sure a {@link SimpleValue} is the correct type.
 */
public class InstanceOfValueValidator
	implements ValueValidator<SimpleValue>
{
	private final Class<?> type;

	public InstanceOfValueValidator(
		Class<?> type
	)
	{
		this.type = type;
	}

	@Override
	public void validate(
		SimpleValue value,
		ValidationMessageCollector collector
	)
	{
		if(value == null) return;

		if(type.isAssignableFrom(value.get().getClass()))
		{
			collector.error()
				.withMessage("Value has the wrong type")
				.done();
		}
	}
}
