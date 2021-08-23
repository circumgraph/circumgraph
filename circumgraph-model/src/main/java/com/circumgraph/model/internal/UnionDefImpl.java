package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.RichIterable;
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
	private final Metadata metadata;

	private ModelDefs defs;

	public UnionDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<OutputTypeDef> types,
		ImmutableList<DirectiveUse> directives,
		Metadata metadata
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.types = types;
		this.directives = directives;
		this.metadata = metadata;
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
	public ListIterable<ObjectDef> getTypes()
	{
		if(defs == null)
		{
			return Lists.immutable.empty();
		}

		return types.collect(s -> defs.getType(s, ObjectDef.class));
	}

	@Override
	public ListIterable<? extends OutputTypeDef> getRawTypes()
	{
		return types;
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
	public <V> Optional<V> getMetadata(MetadataKey<V> key)
	{
		return metadata.getMetadata(key);
	}

	@Override
	public RichIterable<MetadataDef> getDefinedMetadata()
	{
		return metadata.getDefinedMetadata();
	}

	@Override
	public <V> void setRuntimeMetadata(MetadataKey<V> key, V value)
	{
		metadata.setRuntimeMetadata(key, value);
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
		return Objects.hash(description, directives, name, types, metadata);
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
			&& Objects.equals(types, other.types)
			&& Objects.equals(metadata, other.metadata);
	}

	@Override
	public Builder derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			types,
			directives,
			metadata.derive()
		);
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(
			null,
			name,
			null,
			Lists.immutable.empty(),
			Lists.immutable.empty(),
			Metadata.empty()
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
		private final Metadata metadata;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			ImmutableList<OutputTypeDef> types,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.types = types;
			this.directives = directives;
			this.metadata = metadata;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				types,
				directives,
				metadata
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
				directives,
				metadata
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
				directives.newWith(directive),
				metadata
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
				this.directives.newWithAll(directives),
				metadata
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
				directives,
				metadata
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
				directives,
				metadata
			);
		}

		@Override
		public <V> Builder withMetadata(MetadataKey<V> key, V value)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				types,
				directives,
				metadata.withMetadata(key, value)
			);
		}

		@Override
		public Builder withAllMetadata(Iterable<MetadataDef> defs)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				types,
				directives,
				metadata.withAllMetadata(defs)
			);
		}

		@Override
		public UnionDef build()
		{
			return new UnionDefImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				description,
				types,
				directives,
				metadata
			);
		}
	}
}
