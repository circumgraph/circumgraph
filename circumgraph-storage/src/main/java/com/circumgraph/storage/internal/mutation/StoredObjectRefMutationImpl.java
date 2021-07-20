package com.circumgraph.storage.internal.mutation;

import java.util.Objects;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;

public class StoredObjectRefMutationImpl
	implements StoredObjectRefMutation
{
	private final StructuredDef def;
	private final long id;

	public StoredObjectRefMutationImpl(
		StructuredDef def,
		long id
	)
	{
		this.def = def;
		this.id = id;
	}

	@Override
	public StructuredDef getDef()
	{
		return def;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, def);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StoredObjectRefMutationImpl other = (StoredObjectRefMutationImpl) obj;
		return id == other.id
			&& Objects.equals(def, other.def);
	}

	@Override
	public String toString()
	{
		return "StoredObjectRefMutation{id=" + id + ", def=" + def + "}";
	}
}
