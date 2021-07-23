package com.circumgraph.storage.internal.mutation;

import com.circumgraph.storage.mutation.NullMutation;

public class NullMutationImpl
	implements NullMutation
{
	public static final NullMutation INSTANCE = new NullMutationImpl();

	@Override
	public String toString()
	{
		return "NullMutation{}";
	}
}
