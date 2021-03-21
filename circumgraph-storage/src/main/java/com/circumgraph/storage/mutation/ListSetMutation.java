package com.circumgraph.storage.mutation;

import com.circumgraph.storage.internal.mutation.ListSetMutationImpl;
import com.circumgraph.values.Value;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Mutation that sets the value of a list.
 */
public interface ListSetMutation<V extends Value>
	extends ListMutation<V>
{
	/**
	 * Get the values that this list should have when done.
	 *
	 * @return
	 */
	ListIterable<V> getValues();

	/**
	 * Create a mutation that will set the given values.
	 *
	 * @param <V>
	 * @param values
	 * @return
	 */
	static <V extends Value> ListSetMutation<V> create(Iterable<V> values)
	{
		return new ListSetMutationImpl<>(Lists.immutable.ofAll(values));
	}

	/**
	 * Create a mutation that will set the given values.
	 *
	 * @param <V>
	 * @param values
	 * @return
	 */
	@SafeVarargs
	static <V extends Value> ListSetMutation<V> create(V... values)
	{
		return create(Lists.immutable.of(values));
	}
}
