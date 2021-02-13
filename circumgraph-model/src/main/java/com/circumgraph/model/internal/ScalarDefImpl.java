package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.ScalarDef;

/**
 * Implementation of {@link ScalarDef}.
 */
public class ScalarDefImpl
	implements ScalarDef
{
	private final String name;
	private final String description;

	public ScalarDefImpl(
		String name,
		String description
	)
	{
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Optional<String> getDescription()
	{
		return Optional.ofNullable(description);
	}
}
