package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link UnionDef}.
 */
public class UnionDefImpl
	implements UnionDef, HasPreparation
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final String description;
	private final ImmutableList<OutputTypeDef> types;
	private final ImmutableList<DirectiveUse> directives;

	private ModelDefs defs;

	public UnionDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<OutputTypeDef> types,
		ImmutableList<DirectiveUse> directives
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.types = types;
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
	public ListIterable<OutputTypeDef> getTypes()
	{
		return types.collect(s -> defs.getType(s, OutputTypeDef.class));
	}

	@Override
	public ListIterable<String> getTypeNames()
	{
		return types.collect(TypeDef::getName);
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

	@Override
	public boolean isReady()
	{
		return defs != null;
	}

	@Override
	public boolean isAssignableFrom(TypeDef other)
	{
		if(other instanceof UnionDef)
		{
			return this == other;
		}

		for(var type : types)
		{
			if(type.isAssignableFrom(other))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(description, directives, name, types);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		UnionDefImpl other = (UnionDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& Objects.equals(types, other.types);
	}

	@Override
	public Builder derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			types,
			directives
		);
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
		private final ImmutableList<OutputTypeDef> types;
		private final ImmutableList<DirectiveUse> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			ImmutableList<OutputTypeDef> types,
			ImmutableList<DirectiveUse> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.types = types;
			this.directives = directives;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				types,
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
				types,
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
				types,
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
				types,
				this.directives.newWithAll(directives)
			);
		}

		@Override
		public Builder addType(OutputTypeDef value)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				types.newWith(value),
				directives
			);
		}

		@Override
		public Builder addTypes(Iterable<? extends OutputTypeDef> types)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				this.types.newWithAll(types),
				directives
			);
		}

		@Override
		public UnionDef build()
		{
			return new UnionDefImpl(
				sourceLocation,
				name,
				description,
				types,
				directives
			);
		}
	}
}
