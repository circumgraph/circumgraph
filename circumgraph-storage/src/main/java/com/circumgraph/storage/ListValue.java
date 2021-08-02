package com.circumgraph.storage;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.internal.ListValueImpl;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Value representing a list of items.
 */
public interface ListValue<V extends Value>
	extends Value
{
	@Override
	ListDef getDefinition();

	/**
	 * Get the definition for items in this list.
	 *
	 * @return
	 */
	TypeDef getItemDefinition();

	/**
	 * Get the items in this list.
	 *
	 * @return
	 */
	ListIterable<V> items();

	/**
	 * Create an instance of {@link ListValue}.
	 *
	 * @param definition
	 * @param items
	 * @return
	 */
	public static <V extends Value> ListValue<V> create(
		ListDef definition,
		Iterable<? extends V> items
	)
	{
		return new ListValueImpl<>(definition, Lists.immutable.ofAll(items));
	}
}
