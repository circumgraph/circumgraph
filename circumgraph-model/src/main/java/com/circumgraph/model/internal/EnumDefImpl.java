package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link EnumDef}.
 */
public class EnumDefImpl
	implements EnumDef
{
	private final SourceLocation sourceLocation;
	private final String name;
	private final String description;
	private final ImmutableList<EnumValueDef> values;
	private final ImmutableList<DirectiveUse> directives;
	private final Metadata metadata;

	public EnumDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<EnumValueDef> values,
		ImmutableList<DirectiveUse> directives,
		Metadata metadata
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.values = values;
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
	public ListIterable<EnumValueDef> getValues()
	{
		return values;
	}

	@Override
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
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
		return this == other;
	}

	@Override
	public Builder derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			values,
			directives,
			metadata.derive()
		);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(description, directives, name, values, metadata);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		EnumDefImpl other = (EnumDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& Objects.equals(values, other.values)
			&& Objects.equals(metadata, other.metadata);
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
		private final ImmutableList<EnumValueDef> values;
		private final ImmutableList<DirectiveUse> directives;
		private final Metadata metadata;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			ImmutableList<EnumValueDef> values,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.values = values;
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
				values,
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
				values,
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
				values,
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
				values,
				this.directives.newWithAll(directives),
				metadata
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
				directives,
				metadata
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
				values,
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
				values,
				directives,
				metadata.withAllMetadata(defs)
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
				directives,
				metadata
			);
		}
	}
}
