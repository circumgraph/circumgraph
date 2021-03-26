package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.values.Value;

public interface ValueMapper<V extends Value, M extends Mutation>
{
	/**
	 * Get the initial value of this
	 */
	V getInitialValue();

	V applyMutation(
		V previousValue,
		M mutation
	);

	void validate(
		Consumer<ValidationMessage> validationCollector,
		V value
	);
}
