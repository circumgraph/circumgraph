package com.circumgraph.model.internal;

import java.util.Objects;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link DirectiveUse}.
 */
public class DirectiveUseImpl
	implements DirectiveUse
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final ImmutableList<ArgumentUse> arguments;

	public DirectiveUseImpl(
		SourceLocation sourceLocation,
		String name,
		ImmutableList<ArgumentUse> arguments
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
	public ListIterable<ArgumentUse> getArguments()
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
		return "DirectiveUse{name=" + name + ", arguments=" + arguments + "}";
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(null, name, Lists.immutable.empty());
	}

	private static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final ImmutableList<ArgumentUse> arguments;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			ImmutableList<ArgumentUse> arguments
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
		public Builder addArgument(ArgumentUse argument)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				arguments.newWith(argument)
			);
		}

		@Override
		public Builder addArguments(Iterable<? extends ArgumentUse> arguments)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				this.arguments.newWithAll(arguments)
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
