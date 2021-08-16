package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;

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
	public boolean isAssignableFrom(TypeDef other)
	{
		if(other instanceof NonNullDef)
		{
			other = ((NonNullDef) other).getType();
		}

		return this == other;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, description);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ScalarDefImpl other = (ScalarDefImpl) obj;
		return Objects.equals(name, other.name)
			&& Objects.equals(description, other.description);
	}

	@Override
	public String toString()
	{
		return "ScalarDef{name=" + name + ", description=" + description + "}";
	}
}
