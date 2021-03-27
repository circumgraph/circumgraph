package com.circumgraph.storage.internal.mutation;

import java.util.Objects;

import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.Mutation;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link ListSetMutation}.
 */
public class ListSetMutationImpl<M extends Mutation>
	implements ListSetMutation<M>
{
	private final ListIterable<M> values;

	public ListSetMutationImpl(
		ListIterable<M> values
	)
	{
		this.values = values;
	}

	@Override
	public ListIterable<M> getValues()
	{
		return values;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(values);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ListSetMutationImpl other = (ListSetMutationImpl) obj;
		return Objects.equals(values, other.values);
	}

	@Override
	public String toString()
	{
		return "ListSetMutation{values=" + values + "}";
	}
}
