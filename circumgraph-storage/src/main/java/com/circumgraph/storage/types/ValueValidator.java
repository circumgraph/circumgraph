package com.circumgraph.storage.types;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
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
	 * @param validationCollector
	 */
	void validate(
		V value,
		Consumer<ValidationMessage> validationCollector
	);
}
