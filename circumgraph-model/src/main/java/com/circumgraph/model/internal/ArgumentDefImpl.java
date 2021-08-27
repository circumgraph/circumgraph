package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.TypeRef;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link ArgumentDef}.
 */
public class ArgumentDefImpl
	implements ArgumentDef, HasPreparation
{
	private final Location sourceLocation;

	private final String name;
	private final String description;
	private final InputTypeDef type;
	private final ImmutableList<DirectiveUse> directives;
	private final Object defaultValue;

	private FieldDef declaringField;

	private ModelDefs defs;

	public ArgumentDefImpl(
		Location sourceLocation,
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
	public ListIterable<DirectiveUse> getDirectives()
	{
		return directives;
	}

	@Override
	public Optional<Object> getDefaultValue()
	{
		return Optional.ofNullable(defaultValue);
	}

	@Override
	public FieldDef getDeclaringField()
	{
		return declaringField;
	}

	public void setDeclaringField(FieldDef declaringField)
	{
		this.declaringField = declaringField;
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
	public Builder derive()
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
	public int hashCode()
	{
		return Objects.hash(defaultValue, description, directives, name, type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ArgumentDefImpl other = (ArgumentDefImpl) obj;
		return Objects.equals(defaultValue, other.defaultValue)
			&& Objects.equals(description, other.description)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(name, other.name)
			&& Objects.equals(type, other.type);
	}

	@Override
	public String toString()
	{
		return "ArgumentDefImpl{"
			+ "name=" + name
			+ ", type=" + type
			+ ", defaultValue=" + defaultValue
			+ ", description=" + description
			+ ", directives=" + directives
			+ "}";
	}

	public static Builder create(String name)
	{
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
		private final Location sourceLocation;
		private final String name;
		private final String description;
		private final InputTypeDef type;
		private final ImmutableList<DirectiveUse> directives;
		private final Object defaultValue;

		public BuilderImpl(
			Location sourceLocation,
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
		public Builder withDefinedAt(Location sourceLocation)
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
		public Builder withType(String type)
		{
			return withType(TypeRef.create(type));
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
		public Builder withDefaultValue(Object value)
		{
			return new BuilderImpl(
				sourceLocation,
				name,
				description,
				type,
				directives,
				value
			);
		}

		@Override
		public ArgumentDef build()
		{
			return new ArgumentDefImpl(
				Location.automatic(sourceLocation),
				name,
				description,
				type,
				directives,
				defaultValue
			);
		}
	}
}
