package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.MetadataDef;
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
	private final SourceLocation sourceLocation;

	private final String name;
	private final String description;
	private final ImmutableList<DirectiveUse> directives;
	private final ImmutableList<InputFieldDef> fields;
	private final Metadata metadata;

	protected ModelDefs defs;

	public InputObjectDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<InputFieldDef> fields,
		Metadata metadata
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.fields = fields;
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
	public Builder derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			fields,
			directives,
			metadata.derive()
		);
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;

		for(InputFieldDef field : fields)
		{
			((InputFieldDefImpl) field).setDeclaringType(this);
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
	public int hashCode()
	{
		return Objects
			.hash(description, fields, name, directives, metadata);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		InputObjectDefImpl other = (InputObjectDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(fields, other.fields)
			&& Objects.equals(name, other.name)
			&& Objects.equals(directives, other.directives)
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

		private final String id;
		private final String description;

		private final ImmutableList<InputFieldDef> fields;
		private final ImmutableList<DirectiveUse> directives;
		private final Metadata metadata;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<InputFieldDef> fields,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			this.sourceLocation = sourceLocation;
			this.id = id;
			this.description = description;
			this.fields = fields;
			this.directives = directives;
			this.metadata = metadata;
		}

		@Override
		public Builder withDescription(String description)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives,
				metadata
			);
		}

		@Override
		public Builder addField(InputFieldDef field)
		{
			var currentField = fields.detect(f -> f.getName().equals(field.getName()));
			var newFields = (currentField == null ? fields : fields.newWithout(currentField))
				.newWith(field);

			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				newFields,
				directives,
				metadata
			);
		}

		@Override
		public Builder addFields(Iterable<? extends InputFieldDef> fields)
		{
			Builder result = this;
			for(var field : fields)
			{
				result = result.addField(field);
			}
			return result;
		}

		@Override
		public Builder addDirective(DirectiveUse directive)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives.newWith(directive),
				metadata
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
				this.directives.newWithAll(directives),
				metadata
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
				directives,
				metadata
			);
		}

		@Override
		public <V> Builder withMetadata(MetadataKey<V> key, V value)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives,
				metadata.withMetadata(key, value)
			);
		}

		@Override
		public Builder withAllMetadata(Iterable<MetadataDef> defs)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				directives,
				metadata.withAllMetadata(defs)
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
				fields,
				metadata
			);
		}
	}
}
