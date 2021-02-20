package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link ArgumentDef}.
 */
public class ArgumentDefImpl
	implements ArgumentDef, HasPreparation
{
	private final SourceLocation sourceLocation;

	private final String name;
	private final String description;

	private final InputTypeDef type;
	private final boolean nullable;

	private final ImmutableList<DirectiveUse> directives;

	private ModelDefs defs;

	public ArgumentDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		InputTypeDef type,
		boolean nullable,
		ImmutableList<DirectiveUse> directives
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.type = type;
		this.nullable = nullable;
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
	public InputTypeDef getType()
	{
		return defs.getType(type, InputTypeDef.class);
	}

	@Override
	public boolean isNullable()
	{
		return nullable;
	}

	@Override
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;
	}

	public static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final String description;
		private final InputTypeDef type;
		private final boolean nullable;
		private final ImmutableList<DirectiveUse> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			InputTypeDef type,
			boolean nullable,
			ImmutableList<DirectiveUse> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.type = type;
			this.nullable = nullable;
			this.directives = directives;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
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
				type,
				nullable,
				directives
			);
		}

		@Override
		public Builder withType(InputTypeDef type)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				directives
			);
		}

		@Override
		public Builder nullable()
		{
			return withNullable(true);
		}

		@Override
		public Builder withNullable(boolean nullable)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
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
				type,
				nullable,
				directives.newWith(directive)
			);
		}

		@Override
		public ArgumentDef build()
		{
			return new ArgumentDefImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				directives
			);
		}
	}
}
