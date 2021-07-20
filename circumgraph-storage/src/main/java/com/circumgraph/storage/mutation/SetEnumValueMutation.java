package com.circumgraph.storage.mutation;

import com.circumgraph.storage.internal.mutation.SetEnumValueMutationImpl;

/**
 * Mutation to set the value of an enum.
 */
public interface SetEnumValueMutation
	extends Mutation
{
	/**
	 * Get the value to set.
	 *
	 * @return
	 */
	String getValue();

	/**
	 * Create a mutation to set a specific value.
	 *
	 * @param value
	 *   value to set
	 * @return
	 *   mutation
	 */
	static SetEnumValueMutation create(String value)
	{
		return new SetEnumValueMutationImpl(value);
	}
}
