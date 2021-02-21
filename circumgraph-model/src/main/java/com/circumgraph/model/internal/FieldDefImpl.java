package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
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
	private final ImmutableList<DirectiveUse> directives;

	private ModelDefs defs;

	public FieldDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		OutputTypeDef type,
		boolean nullable,
		ImmutableList<ArgumentDef> arguments,
		ImmutableList<DirectiveUse> directives
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
		return defs == null ? type : defs.getType(type, OutputTypeDef.class);
	}

	@Override
	public String getTypeName()
	{
		return type.getName();
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
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
	}

	@Override
	public String toString()
	{
		return "FieldDef{name=" + name
			+ ", type=" + type
			+ ", nullable=" + nullable
			+ ", description=" + description
			+ ", directives=" + directives
			+ ", sourceLocation=" + sourceLocation
			+ "}";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(arguments, description, directives, name, nullable, type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		FieldDefImpl other = (FieldDefImpl) obj;
		return Objects.equals(arguments, other.arguments)
			&& Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& nullable == other.nullable
			&& Objects.equals(type.getName(), other.type.getName());
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
		private final ImmutableList<DirectiveUse> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			OutputTypeDef type,
			boolean nullable,
			ImmutableList<ArgumentDef> arguments,
			ImmutableList<DirectiveUse> directives
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
		public Builder addArguments(Iterable<? extends ArgumentDef> args)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				arguments.newWithAll(args),
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
				arguments,
				directives.newWith(directive)
			);
		}

		@Override
		public Builder addDirectives(
			Iterable<? extends DirectiveUse> directives
		)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				nullable,
				arguments,
				this.directives.newWithAll(directives)
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
