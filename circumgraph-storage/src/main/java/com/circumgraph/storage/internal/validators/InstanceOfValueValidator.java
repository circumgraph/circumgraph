package com.circumgraph.storage.internal.validators;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
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
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(value == null) return;

		if(type.isAssignableFrom(value.get().getClass()))
		{
			validationCollector.accept(ValidationMessage.error()
				.withMessage("Value has the wrong type")
				.build()
			);
		}
	}
}
