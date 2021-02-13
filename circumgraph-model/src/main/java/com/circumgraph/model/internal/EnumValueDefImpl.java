package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.DirectiveDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

public class EnumValueDefImpl
	implements EnumValueDef
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final String description;
	private final ImmutableList<DirectiveDef> directives;

	public EnumValueDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<DirectiveDef> directives
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.directives = directives;
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
	public Optional<String> getDescription()
	{
		return Optional.ofNullable(description);
	}

	@Override
	public ListIterable<DirectiveDef> getDirectives()
	{
		return directives;
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(
			null,
			name,
			null,
			Lists.immutable.empty()
		);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final String description;
		private final ImmutableList<DirectiveDef> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			ImmutableList<DirectiveDef> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.directives = directives;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				directives
			);
		}

		@Override
		public Builder withDescription(String description)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				directives
			);
		}

		@Override
		public Builder addDirective(DirectiveDef directive)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				directives.newWith(directive)
			);
		}

		@Override
		public EnumValueDef build()
		{
			return new EnumValueDefImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				description,
				directives
			);
		}
	}
}
