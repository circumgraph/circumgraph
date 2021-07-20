package com.circumgraph.storage.internal.validators;

import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.SimpleValue;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	public Flux<ValidationMessage> validate(
		SourceLocation location,
		SimpleValue value
	)
	{
		return Mono.fromSupplier(() -> {
			if(value != null && type.isAssignableFrom(value.get().getClass()))
			{
				return ValidationMessage.error()
					.withMessage("Value has the wrong type")
					.build();
			}

			return null;
		}).flux();
	}
}
