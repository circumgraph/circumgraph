package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link InputObjectDef}.
 */
public class InputObjectDefImpl
	implements InputObjectDef, HasPreparation
{
	protected final SourceLocation sourceLocation;

	protected final String name;
	protected final String description;
	protected final ImmutableList<DirectiveUse> directives;
	protected final ImmutableList<InputFieldDef> fields;

	private final MetadataHelper metadata;
	protected ModelDefs defs;

	public InputObjectDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<InputFieldDef> fields
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.fields = fields;
		this.directives = directives;

		this.metadata = new MetadataHelper();
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
	public RichIterable<InputFieldDef> getFields()
	{
		return fields;
	}

	@Override
	public Optional<InputFieldDef> getField(String name)
	{
		return fields.detectOptional(f -> name.equals(f.getName()));
	}

	@Override
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
	}

	@Override
	public boolean isAssignableFrom(TypeDef other)
	{
		if(other instanceof NonNullDef.Input i)
		{
			other = i.getType();
		}

		return other.getName().equals(name);
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;

		for(InputFieldDef field : fields)
		{
			HasPreparation.maybePrepare(field, defs);
		}
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
	public <V> void setMetadata(MetadataKey<V> key, V value)
	{
		metadata.setMetadata(key, value);
	}

	@Override
	public int hashCode()
	{
		return Objects
			.hash(defs, description, fields, name, directives);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		InputObjectDefImpl other = (InputObjectDefImpl) obj;
		return Objects.equals(defs, other.defs)
			&& Objects.equals(description, other.description)
			&& Objects.equals(fields, other.fields)
			&& Objects.equals(name, other.name)
			&& Objects.equals(directives, other.directives);
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
		protected final SourceLocation sourceLocation;

		protected final String id;
		protected final String description;

		protected final ImmutableList<InputFieldDef> fields;
		protected final ImmutableList<DirectiveUse> directives;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<InputFieldDef> fields,
			ImmutableList<DirectiveUse> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.id = id;
			this.description = description;
			this.fields = fields;
			this.directives = directives;
		}

		@Override
		public Builder withDescription(String description)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives
			);
		}

		@Override
		public Builder addField(InputFieldDef field)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields.newWith(field),
				directives
			);
		}

		@Override
		public Builder addFields(Iterable<? extends InputFieldDef> fields)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				this.fields.newWithAll(fields),
				directives
			);
		}

		@Override
		public Builder addDirective(DirectiveUse directive)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives.newWith(directive)
			);
		}

		@Override
		public Builder addDirectives(Iterable<? extends DirectiveUse> directives)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				this.directives.newWithAll(directives)
			);
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives
			);
		}

		@Override
		public InputObjectDef build()
		{
			return new InputObjectDefImpl(
				sourceLocation,
				id,
				description,
				directives,
				fields
			);
		}
	}
}
