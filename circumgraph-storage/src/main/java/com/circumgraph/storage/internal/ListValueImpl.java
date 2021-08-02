package com.circumgraph.storage.internal;

import java.util.Objects;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.Value;

import org.eclipse.collections.api.list.ListIterable;

public class ListValueImpl<V extends Value>
	implements ListValue<V>
{
	private final ListDef definition;
	private final ListIterable<V> items;

	public ListValueImpl(
		ListDef definition,
		ListIterable<V> items
	)
	{
		this.definition = definition;
		this.items = items;
	}

	@Override
	public ListDef getDefinition()
	{
		return definition;
	}

	@Override
	public TypeDef getItemDefinition()
	{
		return definition.getItemType();
	}

	@Override
	public ListIterable<V> items()
	{
		return items;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(definition, items);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ListValueImpl other = (ListValueImpl) obj;
		return Objects.equals(definition, other.definition)
			&& Objects.equals(items, other.items);
	}

	@Override
	public String toString()
	{
		return "ListValueImpl{items=" + items + ", definition=" + definition + "}";
	}
}
