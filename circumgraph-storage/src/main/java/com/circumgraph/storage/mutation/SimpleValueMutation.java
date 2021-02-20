package com.circumgraph.storage.mutation;

import com.circumgraph.storage.internal.mutation.SimpleValueMutationImpl;

/**
 * Mutation for changing a single value.
 */
public interface SimpleValueMutation<V>
	extends Mutation
{
	/**
	 * Get the value to set.
	 *
	 * @return
	 */
	V getValue();

	static <V> SimpleValueMutation<V> create(V value)
	{
		return new SimpleValueMutationImpl<V>(value);
	}
}
