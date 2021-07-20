package com.circumgraph.storage.mutation;

import com.circumgraph.model.OutputTypeDef;

/**
 * Extension to {@link Mutation} that includes information about the type.
 */
public interface TypedMutation
	extends Mutation
{
	/**
	 * Get the type being mutated.
	 */
	OutputTypeDef getDef();
}
