package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

public class EnumDefImpl
	implements EnumDef
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final String description;
	private final ImmutableList<EnumValueDef> values;
	private final ImmutableList<DirectiveUse> directives;

	public EnumDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<EnumValueDef> values,
		ImmutableList<DirectiveUse> directives
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.values = values;
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
	public ListIterable<EnumValueDef> getValues()
	{
		return values;
	}

	@Override
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(
			null,
			name,
			null,
			Lists.immutable.empty(),
			Lists.immutable.empty()
		);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final String description;
		private final ImmutableList<EnumValueDef> values;
		private final ImmutableList<DirectiveUse> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			ImmutableList<EnumValueDef> values,
			ImmutableList<DirectiveUse> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.values = values;
			this.directives = directives;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				values,
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
				values,
				directives
			);
		}

		@Override
		public Builder addDirective(DirectiveUse directive)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				values,
				directives.newWith(directive)
			);
		}

		@Override
		public Builder addValue(EnumValueDef value)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				values.newWith(value),
				directives
			);
		}

		@Override
		public Builder addValues(Iterable<? extends EnumValueDef> values)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				this.values.newWithAll(values),
				directives
			);
		}

		@Override
		public EnumDef build()
		{
			return new EnumDefImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				description,
				values,
				directives
			);
		}
	}
}
