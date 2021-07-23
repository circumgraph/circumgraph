package com.circumgraph.storage.mutation;

import com.circumgraph.storage.internal.mutation.NullMutationImpl;

/**
 * Mutation used to set a value to {@code null}.
 */
public interface NullMutation
	extends Mutation
{
	static NullMutation create()
	{
		return NullMutationImpl.INSTANCE;
	}
}
