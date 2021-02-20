package com.circumgraph.storage.internal.mutation;

import java.util.Objects;

import com.circumgraph.storage.mutation.SimpleValueMutation;

/**
 * Implementation of {@link SimpleValueMutation}.
 */
public class SimpleValueMutationImpl<V>
	implements SimpleValueMutation<V>
{
	private final V value;

	public SimpleValueMutationImpl(V value)
	{
		this.value = value;
	}

	@Override
	public V getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		SimpleValueMutationImpl other = (SimpleValueMutationImpl) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return "SimpleValueMutation{value=" + value + "}";
	}
}
