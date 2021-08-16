package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;

/**
 * Implementation of {@link TypeRef}.
 */
public class TypeRefImpl
	implements TypeRef
{
	private final String id;

	public TypeRefImpl(
		String id
	)
	{
		this.id = id;
	}

	@Override
	public String getName()
	{
		return id;
	}

	@Override
	public Optional<String> getDescription()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAssignableFrom(TypeDef other)
	{
		return id.equals(other.getName());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		TypeRefImpl other = (TypeRefImpl) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString()
	{
		return "TypeRef{" + id + "}";
	}
}
