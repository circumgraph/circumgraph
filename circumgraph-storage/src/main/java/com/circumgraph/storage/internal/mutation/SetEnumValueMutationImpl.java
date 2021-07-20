package com.circumgraph.storage.internal.mutation;

import java.util.Objects;

import com.circumgraph.storage.mutation.SetEnumValueMutation;

/**
 * Implementation of {@link SetEnumValueMutation}.
 */
public class SetEnumValueMutationImpl
	implements SetEnumValueMutation
{
	private final String value;

	public SetEnumValueMutationImpl(String value)
	{
		this.value = value;
	}

	@Override
	public String getValue()
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
		SetEnumValueMutationImpl other = (SetEnumValueMutationImpl) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return "SetEnumValueMutation{value=" + value + "}";
	}
}
