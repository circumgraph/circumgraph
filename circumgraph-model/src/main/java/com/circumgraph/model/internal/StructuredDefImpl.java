package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public abstract class StructuredDefImpl
	implements StructuredDef, HasPreparation
{
	protected final SourceLocation sourceLocation;

	protected final String name;
	protected final String description;
	protected final ImmutableList<TypeDef> implementsTypes;
	protected final ImmutableList<DirectiveUse> directives;

	protected final ImmutableList<FieldDef> directFields;
	protected ImmutableMap<String, FieldDef> fields;

	protected ModelDefs defs;

	public StructuredDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<TypeDef> implementsTypes,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<FieldDef> fields
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.directFields = fields;
		this.implementsTypes = implementsTypes;
		this.directives = directives;
	}

	@Override
	public SourceLocation getSourceLocation()
	{
		return sourceLocation;
	}

	@Override
	public ListIterable<String> getImplementsNames()
	{
		return implementsTypes.collect(TypeDef::getName);
	}

	@Override
	public ListIterable<InterfaceDef> getImplements()
	{
		return implementsTypes.collect(s -> defs.getType(s, InterfaceDef.class));
	}

	@Override
	public boolean hasImplements(String name)
	{
		return implementsTypes.containsBy(TypeDef::getName, name);
	}

	@Override
	public boolean findImplements(String name)
	{
		var checked = Sets.mutable.<String>empty();
		var stack = getImplements().toStack();
		while(! stack.isEmpty())
		{
			var def = stack.pop();
			if(def.getName().equals(name))
			{
				return true;
			}

			for(var subDef : def.getImplements())
			{
				if(checked.add(subDef.getName()))
				{
					stack.push(subDef);
				}
			}
		}

		return false;
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
	public RichIterable<FieldDef> getFields()
	{
		return fields;
	}

	@Override
	public RichIterable<FieldDef> getDirectFields()
	{
		return directFields;
	}

	@Override
	public Optional<FieldDef> getField(String name)
	{
		return Optional.ofNullable(fields.get(name));
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

		for(FieldDef field : directFields)
		{
			HasPreparation.maybePrepare(field, defs);
		}

		MutableMap<String, FieldDef> fields = directFields.toMap(FieldDef::getName, v -> v);
		getImplements().forEach(def -> {
			HasPreparation.maybePrepare(def, defs);

			collectFields(def, fields);
		});

		this.fields = fields.toImmutable();
	}

	@Override
	public boolean isReady()
	{
		return defs != null;
	}

	private void collectFields(StructuredDef def, MutableMap<String, FieldDef> fields)
	{
		for(FieldDef field : def.getDirectFields())
		{
			if(fields.contains(field.getName()))
			{
				continue;
			}

			fields.put(field.getName(), field);
		}

		def.getImplements().forEach(i -> collectFields(i, fields));
	}

	@Override
	public int hashCode()
	{
		return Objects
			.hash(defs, description, directFields, name, implementsTypes, directives);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StructuredDefImpl other = (StructuredDefImpl) obj;
		return Objects.equals(defs, other.defs)
			&& Objects.equals(description, other.description)
			&& Objects.equals(directFields, other.directFields)
			&& Objects.equals(name, other.name)
			&& Objects.equals(implementsTypes, other.implementsTypes)
			&& Objects.equals(directives, other.directives);
	}

	public abstract static class AbstractBuilder<B extends Builder<B>>
		implements Builder<B>
	{
		protected final SourceLocation sourceLocation;

		protected final String id;
		protected final String description;

		protected final ImmutableList<FieldDef> fields;
		protected final ImmutableList<TypeDef> implementsTypes;

		protected final ImmutableList<DirectiveUse> directives;

		public AbstractBuilder(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives
		)
		{
			this.sourceLocation = sourceLocation;
			this.id = id;
			this.description = description;
			this.fields = fields;
			this.implementsTypes = implementsTypes;
			this.directives = directives;
		}

		protected abstract B create(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives
		);

		@Override
		public B withDescription(String description)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives
			);
		}

		@Override
		public B addImplements(String typeId)
		{
			return addImplements(TypeRef.create(typeId));
		}

		@Override
		public B addImplements(TypeRef type)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes.newWith(type),
				directives
			);
		}

		@Override
		public B addImplementsAll(Iterable<String> types)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes.newWithAll(
					Lists.immutable.ofAll(types).collect(TypeRef::create)
				),
				directives
			);
		}

		@Override
		public B addField(FieldDef field)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields.newWith(field),
				implementsTypes,
				directives
			);
		}

		@Override
		public B addFields(Iterable<? extends FieldDef> fields)
		{
			return create(
				sourceLocation,
				id,
				description,
				this.fields.newWithAll(fields),
				implementsTypes,
				directives
			);
		}

		@Override
		public B addDirective(DirectiveUse directive)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives.newWith(directive)
			);
		}

		@Override
		public B addDirectives(Iterable<? extends DirectiveUse> directives)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				this.directives.newWithAll(directives)
			);
		}

		@Override
		public B withSourceLocation(SourceLocation sourceLocation)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives
			);
		}
	}
}
