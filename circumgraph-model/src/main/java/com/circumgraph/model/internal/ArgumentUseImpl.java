package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.circumgraph.model.ArgumentUse;

/**
 * Implementation of {@link ArgumentUse}.
 */
public class ArgumentUseImpl
	implements ArgumentUse
{
	private final String name;
	private final Object value;

	public ArgumentUseImpl(
		String name,
		Object value
	)
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	@Override
	public OptionalDouble getValueAsDouble()
	{
		return value instanceof Number ? OptionalDouble.of(((Number) value).doubleValue()) : OptionalDouble.empty();
	}

	@Override
	public OptionalInt getValueAsInt()
	{
		return value instanceof Number ? OptionalInt.of(((Number) value).intValue()) : OptionalInt.empty();
	}

	@Override
	public OptionalLong getValueAsLong()
	{
		return value instanceof Number ? OptionalLong.of(((Number) value).longValue()) : OptionalLong.empty();
	}

	@Override
	public Optional<String> getValueAsString()
	{
		return value instanceof String ? Optional.of((String) value) : Optional.empty();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ArgumentUseImpl other = (ArgumentUseImpl) obj;
		return Objects.equals(name, other.name)
			&& Objects.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return "ArgumentUse{name=" + name + ", value=" + value + "}";
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(name, null);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final String name;
		private final Object value;

		public BuilderImpl(
			String name,
			Object value
		)
		{
			this.name = name;
			this.value = value;
		}

		@Override
		public Builder withValue(Object value)
		{
			return new BuilderImpl(
				name,
				value
			);
		}

		@Override
		public ArgumentUse build()
		{
			return new ArgumentUseImpl(
				name,
				value
			);
		}
	}
}
