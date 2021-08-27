package com.circumgraph.storage.types;

import com.circumgraph.model.Location;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Value;

import reactor.core.publisher.Flux;

/**
 * Validator for a value.
 */
public interface ValueValidator<V extends Value>
{
	/**
	 * Empty value validator.
	 */
	static ValueValidator<?> EMPTY = (location, v) -> Flux.empty();

	/**
	 * Validate this value.
	 *
	 * @param value
	 * @param location
	 */
	Flux<ValidationMessage> validate(
		Location location,
		V value
	);

	/**
	 * Get an empty validator.
	 *
	 * @param <V>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <V extends Value> ValueValidator<V> empty()
	{
		return (ValueValidator<V>) EMPTY;
	}
}
