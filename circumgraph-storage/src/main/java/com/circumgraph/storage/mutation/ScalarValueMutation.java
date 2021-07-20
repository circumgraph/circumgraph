package com.circumgraph.storage.mutation;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.internal.mutation.ScalarValueMutationImpl;

/**
 * Mutation for changing a single value.
 */
public interface ScalarValueMutation<V>
	extends Mutation
{
	/**
	 * Get the value to set.
	 *
	 * @return
	 */
	V getValue();

	static <V> ScalarValueMutation<V> create(ScalarDef def, V value)
	{
		return new ScalarValueMutationImpl<V>(def, value);
	}

	static ScalarValueMutation<String> createString(String value)
	{
		return create(ScalarDef.STRING, value);
	}

	static ScalarValueMutation<Boolean> createBoolean(boolean value)
	{
		return create(ScalarDef.BOOLEAN, value);
	}

	static ScalarValueMutation<Double> createFloat(double value)
	{
		return create(ScalarDef.FLOAT, value);
	}

	static ScalarValueMutation<Integer> createInt(int value)
	{
		return create(ScalarDef.INT, value);
	}
}
