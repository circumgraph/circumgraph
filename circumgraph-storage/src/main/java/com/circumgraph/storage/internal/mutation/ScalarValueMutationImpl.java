package com.circumgraph.storage.internal.mutation;

import java.util.Objects;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.ScalarValueMutation;

/**
 * Implementation of {@link ScalarValueMutation}.
 */
public class ScalarValueMutationImpl<V>
	implements ScalarValueMutation<V>
{
	private final ScalarDef def;
	private final V value;

	public ScalarValueMutationImpl(ScalarDef def, V value)
	{
		this.def = def;
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
		return Objects.hash(def, value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ScalarValueMutationImpl other = (ScalarValueMutationImpl) obj;
		return Objects.equals(def, other.def)
			&& Objects.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return "ScalarValueMutation{value=" + value + ", def=" + def + "}";
	}
}
