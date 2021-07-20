package com.circumgraph.storage.types;

import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.values.Value;

import reactor.core.publisher.Flux;

/**
 * Validator for a value.
 */
public interface ValueValidator<V extends Value>
{
	/**
	 * Validate this value.
	 *
	 * @param value
	 * @param location
	 */
	Flux<ValidationMessage> validate(
		SourceLocation location,
		V value
	);
}
