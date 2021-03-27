package com.circumgraph.storage.mutation;

import com.circumgraph.storage.internal.mutation.ListSetMutationImpl;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Mutation that sets the value of a list.
 */
public interface ListSetMutation<M extends Mutation>
	extends ListMutation<M>
{
	/**
	 * Get the values that this list should have when done.
	 *
	 * @return
	 */
	ListIterable<M> getValues();

	/**
	 * Create a mutation that will set the given values.
	 *
	 * @param <V>
	 * @param values
	 * @return
	 */
	static <M extends Mutation> ListSetMutation<M> create(Iterable<M> values)
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
	static <M extends Mutation> ListSetMutation<M> create(M... values)
	{
		return create(Lists.immutable.of(values));
	}
}
