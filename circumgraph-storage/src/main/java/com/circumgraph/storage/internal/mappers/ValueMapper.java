package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.validation.ValidationMessageCollector;
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
		ValidationMessageCollector collector,
		V value
	);
}
