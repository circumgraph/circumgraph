package com.circumgraph.storage.types;

import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.values.Value;

/**
 * Validator for a value.
 */
public interface ValueValidator<V extends Value>
{
	/**
	 * Validate this value.
	 *
	 * @param value
	 */
	void validate(V value, ValidationMessageCollector collector);
}
