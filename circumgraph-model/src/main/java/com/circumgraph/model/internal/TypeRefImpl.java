package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.TypeRef;

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
}
