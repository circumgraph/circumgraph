package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueValidator;

import reactor.core.publisher.Flux;

public class TestValidators
{
	private TestValidators()
	{
	}

	/**
	 * Get a validator that always fails with the code {@code test}.
	 *
	 * @param <V>
	 * @return
	 */
	public static <V extends Value> ValueValidator<V> failing()
	{
		return (loc, v) -> Flux.just(ValidationMessageType.error()
			.withCode("test")
			.withMessage("No values allowed")
			.build()
			.toMessage()
			.withLocation(loc)
			.build()
		);
	}
}
