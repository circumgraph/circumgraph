package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.validation.ModelValidation;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link FieldDef}.
 */
public class FieldDefImpl
	implements FieldDef, HasPreparation
{
	private final SourceLocation sourceLocation;

	private final String name;
	private final String description;
	private final boolean nullable;

	private final OutputTypeDef type;
	private final ImmutableList<ArgumentDef> arguments;
	private final ImmutableList<DirectiveDef> directives;

	private ModelDefs defs;

	public FieldDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		OutputTypeDef type,
		boolean nullable,
		ImmutableList<ArgumentDef> arguments,
		ImmutableList<DirectiveDef> directives
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.type = type;
		this.nullable = nullable;
		this.arguments = arguments;
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
	public OutputTypeDef getType()
	{
		return defs.getType(type, OutputTypeDef.class);
	}

	@Override
	public boolean isNullable()
	{
		return nullable;
	}

	@Override
	public ListIterable<ArgumentDef> getArguments()
	{
		return arguments;
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;
	}

	@Override
	public ListIterable<DirectiveDef> getDirectives()
	{
		return directives;
	}

	public static Builder create(String name)
	{
		ModelValidation.requireValidFieldName(name);

		return new BuilderImpl(
			null,
			name,
			null,
			null,
			false,
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
		private final OutputTypeDef type;
		private final boolean nullable;
		private final ImmutableList<ArgumentDef> arguments;
		private final ImmutableList<DirectiveDef> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			OutputTypeDef type,
			boolean nullable,
			ImmutableList<ArgumentDef> arguments,
			ImmutableList<DirectiveDef> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.type = type;
			this.nullable = nullable;
			this.arguments = arguments;
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
				arguments,
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
				arguments,
				directives
			);
		}

		@Override
		public Builder withType(String type)
		{
			return withType(TypeRef.create(type));
		}

		@Override
		public Builder withType(OutputTypeDef type)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				arguments,
				directives
			);
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
				arguments,
				directives
			);
		}

		@Override
		public Builder nullable()
		{
			return withNullable(true);
		}

		@Override
		public Builder addArgument(ArgumentDef arg)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				arguments.newWith(arg),
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
				type,
				nullable,
				arguments,
				directives.newWith(directive)
			);
		}

		@Override
		public FieldDef build()
		{
			Objects.requireNonNull(type, "type must be specified");

			return new FieldDefImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				description,
				type,
				nullable,
				arguments,
				directives
			);
		}
	}
}
