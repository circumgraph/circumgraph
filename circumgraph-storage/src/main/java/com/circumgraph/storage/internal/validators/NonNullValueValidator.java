package com.circumgraph.storage.internal.validators;

import com.circumgraph.model.Location;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Validator for dealing with anything that can not be null.
 */
public class NonNullValueValidator
	implements ValueValidator<Value>
{
	private static NonNullValueValidator INSTANCE = new NonNullValueValidator();

	private static ValidationMessageType ERROR = ValidationMessageType.error()
		.withCode("storage:validation:null")
		.withMessage("Value can not be null")
		.build();

	@SuppressWarnings("unchecked")
	public static <V extends Value> ValueValidator<V> instance()
	{
		return (ValueValidator<V>) INSTANCE;
	}

	@Override
	public Flux<ValidationMessage> validate(
		Location location,
		Value value
	)
	{
		return Mono.fromSupplier(() -> {
			if(value == null)
			{
				return ERROR.toMessage()
					.withLocation(location)
					.build();
			}

			return null;
		}).flux();
	}
}
