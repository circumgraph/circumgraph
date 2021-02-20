package com.circumgraph.model.internal;

import java.util.Objects;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

public class DirectiveUseImpl
	implements DirectiveUse
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final ImmutableList<Argument> arguments;

	public DirectiveUseImpl(
		SourceLocation sourceLocation,
		String name,
		ImmutableList<Argument> arguments
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.arguments = arguments;
	}

	@Override
	public SourceLocation getSourceLocation()
	{
		return sourceLocation;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public ListIterable<Argument> getArguments()
	{
		return arguments;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(arguments, name);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		DirectiveUseImpl other = (DirectiveUseImpl) obj;
		return Objects.equals(arguments, other.arguments)
			&& Objects.equals(name, other.name);
	}

	@Override
	public String toString()
	{
		return "DirectiveUse{arguments=" + arguments + ", name=" + name + "}";
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(null, name, Lists.immutable.empty());
	}

	private static class ArgumentImpl
		implements Argument
	{
		private final String name;
		private final Object value;

		public ArgumentImpl(String name, Object value)
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
			ArgumentImpl other = (ArgumentImpl) obj;
			return Objects.equals(name, other.name)
				&& Objects.equals(value, other.value);
		}

		@Override
		public String toString()
		{
			return "DirectiveUse.Argument{name=" + name + ", value=" + value + "}";
		}
	}

	private static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final ImmutableList<Argument> arguments;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			ImmutableList<Argument> arguments
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.arguments = arguments;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				arguments
			);
		}

		@Override
		public Builder addArgument(String name, Object value)
		{
			return new BuilderImpl(
				sourceLocation,
				this.name,
				arguments.newWith(new ArgumentImpl(name, value))
			);
		}

		@Override
		public DirectiveUse build()
		{
			return new DirectiveUseImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				arguments
			);
		}
	}
}
