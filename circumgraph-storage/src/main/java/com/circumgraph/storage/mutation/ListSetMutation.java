package com.circumgraph.storage.mutation;

import com.circumgraph.values.Value;

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
}
