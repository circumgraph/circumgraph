package com.circumgraph.storage.internal.validators;

import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.Value;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Validator for dealing with anything that can not be null.
 */
public class NonNullValueValidator
	implements ValueValidator<Value>
{
	private static NonNullValueValidator INSTANCE = new NonNullValueValidator();

	@SuppressWarnings("unchecked")
	public static <V extends Value> ValueValidator<V> instance()
	{
		return (ValueValidator<V>) INSTANCE;
	}

	@Override
	public Flux<ValidationMessage> validate(
		SourceLocation location,
		Value value
	)
	{
		return Mono.fromSupplier(() -> {
			if(value == null)
			{
				return ValidationMessage.error()
					.withLocation(location)
					.withCode("storage:non-null-value")
					.withMessage("Value can not be null")
					.build();
			}

			return null;
		}).flux();
	}
}
