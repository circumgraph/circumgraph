package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.SetIterable;

/**
 * Implementation of {@link StructuredDef}.
 */
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

	protected final Metadata metadata;
	protected ModelDefs defs;

	public StructuredDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<TypeDef> implementsTypes,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<FieldDef> fields,
		Metadata metadata
	)
	{
		this.sourceLocation = sourceLocation;
		this.name = name;
		this.description = description;
		this.directFields = fields;
		this.implementsTypes = implementsTypes;
		this.directives = directives;
		this.metadata = metadata;
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
	public SetIterable<InterfaceDef> getAllImplements()
	{
		var result = Sets.mutable.ofAll(getImplements());

		var stack = getImplements().toStack();
		while(! stack.isEmpty())
		{
			var def = stack.pop();
			for(var subDef : def.getImplements())
			{
				if(result.add(subDef))
				{
					stack.push(subDef);
				}
			}
		}

		return result;
	}

	@Override
	public boolean findImplements(String name)
	{
		return findImplements(t -> Objects.equals(name, t.getName())).isPresent();
	}

	@Override
	public Optional<InterfaceDef> findImplements(
		Predicate<InterfaceDef> predicate
	)
	{
		var checked = Sets.mutable.<String>empty();
		var stack = getImplements().toStack();
		while(! stack.isEmpty())
		{
			var def = stack.pop();
			if(predicate.test(def))
			{
				return Optional.of(def);
			}

			for(var subDef : def.getImplements())
			{
				if(checked.add(subDef.getName()))
				{
					stack.push(subDef);
				}
			}
		}

		return Optional.empty();
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
	public Optional<FieldDef> pickField(String path)
	{
		int idx = path.indexOf('.');
		if(idx <= 0)
		{
			// No separators, get the field directly
			return getField(path);
		}

		// Get the field in this def and then try to descend
		var ownField = getField(path.substring(0, idx));
		if(ownField.isPresent() && ownField.get() instanceof StructuredDef d)
		{
			return d.pickField(path.substring(idx + 1));
		}

		return Optional.empty();
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

		// Collect all the indirect fields
		for(var field : directFields)
		{
			HasPreparation.maybePrepare(field, defs);
			((FieldDefImpl) field).setDeclaringType(this);
		}

		MutableMap<String, FieldDef> fields = Maps.mutable.empty();
		collectFields(this, fields);
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
			if(fields.containsKey(field.getName()))
			{
				continue;
			}

			fields.put(field.getName(), field);
		}

		def.getImplementsNames().forEach(name -> {
			var i = defs.getType(name, InterfaceDef.class);
			collectFields(i, fields);
		});
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
		return Objects.hash(
			description,
			directFields,
			name,
			implementsTypes,
			directives,
			metadata
		);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StructuredDefImpl other = (StructuredDefImpl) obj;
		return Objects.equals(description, other.description)
			&& Objects.equals(directFields, other.directFields)
			&& Objects.equals(name, other.name)
			&& Objects.equals(implementsTypes, other.implementsTypes)
			&& Objects.equals(directives, other.directives)
			&& Objects.equals(metadata, other.metadata);
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
		protected final Metadata metadata;

		public AbstractBuilder(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			this.sourceLocation = sourceLocation;
			this.id = id;
			this.description = description;
			this.fields = fields;
			this.implementsTypes = implementsTypes;
			this.directives = directives;
			this.metadata = metadata;
		}

		protected abstract B create(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
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
				directives,
				metadata
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
				directives,
				metadata
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
				directives,
				metadata
			);
		}

		@Override
		public B addField(FieldDef field)
		{
			var currentField = fields.detect(f -> f.getName().equals(field.getName()));
			var newFields = (currentField == null ? fields : fields.newWithout(currentField))
				.newWith(field);

			return create(
				sourceLocation,
				id,
				description,
				newFields,
				implementsTypes,
				directives,
				metadata
			);
		}

		@Override
		@SuppressWarnings("unchecked")
		public B addFields(Iterable<? extends FieldDef> fields)
		{
			var result = (B) this;
			for(var field : fields)
			{
				result = result.addField(field);
			}
			return result;
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
				directives.newWith(directive),
				metadata
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
				this.directives.newWithAll(directives),
				metadata
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
				directives,
				metadata
			);
		}

		@Override
		public <V> B withMetadata(MetadataKey<V> key, V value)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives,
				metadata.withMetadata(key, value)
			);
		}

		@Override
		public B withAllMetadata(Iterable<MetadataDef> defs)
		{
			return create(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives,
				metadata.withAllMetadata(defs)
			);
		}
	}
}
