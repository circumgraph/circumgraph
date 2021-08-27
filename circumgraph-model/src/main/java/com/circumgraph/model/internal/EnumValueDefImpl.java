package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.MetadataKey;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link EnumValueDef}.
 */
public class EnumValueDefImpl
	implements EnumValueDef
{
	private final Location sourceLocation;
	private final String name;
	private final String description;
	private final ImmutableList<DirectiveUse> directives;
	private final Metadata metadata;

	public EnumValueDefImpl(
		Location sourceLocation,
		String name,
		String description,
		ImmutableList<DirectiveUse> directives,
		Metadata metadata
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.directives = directives;
		this.metadata = metadata;
	}

	@Override
	public Location getDefinedAt()
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
	public Builder derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			directives,
			metadata.derive()
		);
	}

	@Override
	public String toString()
	{
		return "EnumValueDefImpl{"
			+ "name=" + name
			+ ", description=" + description
			+ ", directives=" + directives
			+ "}";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(description, directives, name, metadata);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		EnumValueDefImpl other = (EnumValueDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& Objects.equals(metadata, other.metadata);
	}

	public static Builder create(String name)
	{
		return new BuilderImpl(
			null,
			name,
			null,
			Lists.immutable.empty(),
			Metadata.empty()
		);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final Location sourceLocation;
		private final String name;
		private final String description;
		private final ImmutableList<DirectiveUse> directives;
		private final Metadata metadata;

		public BuilderImpl(
			Location sourceLocation,
			String name,
			String description,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.directives = directives;
			this.metadata = metadata;
		}

		@Override
		public Builder withDefinedAt(Location sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
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
				this.directives.newWithAll(directives),
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
				directives,
				metadata.withAllMetadata(defs)
			);
		}

		@Override
		public EnumValueDef build()
		{
			return new EnumValueDefImpl(
				Location.automatic(sourceLocation),
				name,
				description,
				directives,
				metadata
			);
		}
	}
}
