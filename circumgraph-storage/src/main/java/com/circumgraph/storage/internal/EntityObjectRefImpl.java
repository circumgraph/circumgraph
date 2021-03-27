package com.circumgraph.storage.internal;

import java.util.Objects;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.EntityObjectRef;

/**
 * Implementation of {@link EntityObjectRef}.
 */
public class EntityObjectRefImpl
	implements EntityObjectRef
{
	private final StructuredDef def;
	private final long id;

	public EntityObjectRefImpl(
		StructuredDef def,
		long id
	)
	{
		this.def = def;
		this.id = id;
	}

	@Override
	public TypeDef getDefinition()
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
		return Objects.hash(def, id);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		EntityObjectRefImpl other = (EntityObjectRefImpl) obj;
		return Objects.equals(def, other.def)
			&& id == other.id;
	}

	@Override
	public String toString()
	{
		return "EntityObjectRef{id=" + id + ", def=" + def + "}";
	}
}
