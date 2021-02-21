package com.circumgraph.model.internal;

import java.util.Objects;
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

	@Override
	public String toString()
	{
		return "ScalarDef{name=" + name + ", description=" + description + "}";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(description, name);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ScalarDefImpl other = (ScalarDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(name, other.name);
	}


}
