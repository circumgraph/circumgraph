package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.validation.ModelValidation;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link InputFieldDef}.
 */
public class InputFieldDefImpl
	implements InputFieldDef, HasPreparation
{
	private final SourceLocation sourceLocation;

	private final String name;
	private final String description;

	private final InputTypeDef type;
	private final ImmutableList<DirectiveUse> directives;

	private final MetadataHelper metadata;
	private final Object defaultValue;

	private ModelDefs defs;

	public InputFieldDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		InputTypeDef type,
		ImmutableList<DirectiveUse> directives,
		Object defaultValue
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.type = type;
		this.directives = directives;
		this.defaultValue = defaultValue;

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
	public InputTypeDef getType()
	{
		return defs == null ? type : defs.getType(type, InputTypeDef.class);
	}

	@Override
	public String getTypeName()
	{
		return type.getName();
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;

		HasPreparation.maybePrepare(getType(), defs);
	}

	@Override
	public boolean isReady()
	{
		return defs != null;
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
	public <V> void setMetadata(MetadataKey<V> key, V value)
	{
		metadata.setMetadata(key, value);
	}

	@Override
	public String toString()
	{
		return "InputFieldDef{name=" + name
			+ ", type=" + type
			+ ", description=" + description
			+ ", directives=" + directives
			+ ", sourceLocation=" + sourceLocation
			+ "}";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
			defaultValue, description, directives, name, sourceLocation, type
		);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		InputFieldDefImpl other = (InputFieldDefImpl) obj;
		return Objects.equals(defaultValue, other.defaultValue)
			&& Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& Objects.equals(sourceLocation, other.sourceLocation)
			&& Objects.equals(type, other.type);
	}

	public static Builder create(String name)
	{
		ModelValidation.requireValidFieldName(name);

		return new BuilderImpl(
			null,
			name,
			null,
			null,
			Lists.immutable.empty(),
			null
		);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final SourceLocation sourceLocation;
		private final String name;
		private final String description;
		private final InputTypeDef type;
		private final ImmutableList<DirectiveUse> directives;
		private final Object defaultValue;

		public BuilderImpl(
			SourceLocation sourceLocation,
			String name,
			String description,
			InputTypeDef type,
			ImmutableList<DirectiveUse> directives,
			Object defaultValue
		)
		{
			this.sourceLocation = sourceLocation;
			this.name = name;
			this.description = description;
			this.type = type;
			this.directives = directives;
			this.defaultValue = defaultValue;
		}

		@Override
		public Builder withSourceLocation(SourceLocation sourceLocation)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				directives,
				defaultValue
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
				directives,
				defaultValue
			);
		}

		@Override
		public Builder withType(String type)
		{
			return withType(TypeRef.create(type));
		}

		@Override
		public Builder withType(InputTypeDef type)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				directives,
				defaultValue
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
				directives.newWith(directive),
				defaultValue
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
				this.directives.newWithAll(directives),
				defaultValue
			);
		}

		@Override
		public Builder withDefaultValue(Object defaultValue)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				directives,
				defaultValue
			);
		}

		@Override
		public InputFieldDef build()
		{
			Objects.requireNonNull(type, "type must be specified");

			return new InputFieldDefImpl(
				SourceLocation.automatic(sourceLocation),
				name,
				description,
				type,
				directives,
				defaultValue
			);
		}
	}
}
