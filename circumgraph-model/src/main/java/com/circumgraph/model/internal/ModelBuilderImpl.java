package com.circumgraph.model.internal;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasSourceLocation;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.Model.Builder;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

/**
 * Implementation of {@link Model.Builder}.
 */
public class ModelBuilderImpl
	implements Model.Builder
{
	private final ImmutableSet<TypeDef> types;

	public ModelBuilderImpl(
		ImmutableSet<TypeDef> types
	)
	{
		this.types = types;
	}

	@Override
	public Model.Builder addType(TypeDef type)
	{
		return new ModelBuilderImpl(
			types.newWith(type)
		);
	}

	@Override
	public Builder addSchema(Schema schema)
	{
		return new ModelBuilderImpl(
			types.newWithAll(schema.getTypes())
		);
	}

	@Override
	public Model build()
	{
		// Go through and merge all types
		MutableMap<String, TypeDef> typeMap = Maps.mutable.empty();

		for(TypeDef def : types)
		{
			TypeDef current = typeMap.get(def.getName());
			if(current == null)
			{
				typeMap.put(def.getName(), def);
			}
			else
			{
				typeMap.put(def.getName(), merge(current, def));
			}
		}

		// TODO: Validate all the types


		// Prepare and create model
		ImmutableMap<String, TypeDef> types = typeMap.toImmutable();
		ModelDefs defs = types::get;

		for(TypeDef type : types)
		{
			if(type instanceof HasPreparation)
			{
				((HasPreparation) type).prepare(defs);
			}
		}

		return new ModelImpl(types);
	}

	public static ModelBuilderImpl create()
	{
		return new ModelBuilderImpl(Sets.immutable.of(
			ScalarDef.BOOLEAN,
			ScalarDef.FLOAT,
			ScalarDef.INT,
			ScalarDef.STRING,
			ScalarDef.ID
		));
	}

	private TypeDef merge(TypeDef current, TypeDef extension)
	{
		if(current instanceof ObjectDef)
		{
			if(extension instanceof ObjectDef)
			{
				return mergeObject((ObjectDef) current, (ObjectDef) extension);
			}
			else
			{
				throw incompatibleException(current, extension);
			}
		}
		else if(current instanceof InterfaceDef)
		{
			if(extension instanceof InterfaceDef)
			{
				return mergeInterface((InterfaceDef) current, (InterfaceDef) extension);
			}
			else
			{
				throw incompatibleException(current, extension);
			}
		}

		throw incompatibleException(current, extension);
	}

	/**
	 * Create an exception that indicates incompatible types.
	 *
	 * @param current
	 * @param extension
	 * @return
	 */
	private ModelException incompatibleException(TypeDef current, TypeDef extension)
	{
		return new ModelException(
			String.format(
				"Could not merge: %s (at %s) has a different type than previously defined at %s",
				extension.getName(),
				toLocation(extension),
				toLocation(current)
			)
		);
	}

	/**
	 * Merge two object definitions.
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	private ObjectDef mergeObject(ObjectDef d1, ObjectDef d2)
	{
		return new ObjectDefImpl(
			d1.getSourceLocation(),
			d1.getName(),
			d1.getDescription().orElse(d2.getDescription().orElse(null)),
			mergeImplements(d1.getImplementsNames(), d2.getImplementsNames()),
			mergeDirectives(d1.getDirectives(), d2.getDirectives()),
			mergeFields(d1.getDirectFields(), d2.getDirectFields())
		);
	}

	private InterfaceDef mergeInterface(InterfaceDef d1, InterfaceDef d2)
	{
		return new InterfaceDefImpl(
			d1.getSourceLocation(),
			d1.getName(),
			d1.getDescription().orElse(d2.getDescription().orElse(null)),
			mergeImplements(d1.getImplementsNames(), d2.getImplementsNames()),
			mergeDirectives(d1.getDirectives(), d2.getDirectives()),
			mergeFields(d1.getDirectFields(), d2.getDirectFields())
		);
	}

	private ImmutableList<TypeDef> mergeImplements(
		RichIterable<String> i1,
		RichIterable<String> i2
	)
	{
		MutableSet<String> result = Sets.mutable.ofAll(i1);
		result.addAllIterable(i2);
		return result.toList()
			.<TypeDef>collect(TypeRef::create)
			.toImmutable();
	}

	private ImmutableList<DirectiveUse> mergeDirectives(
		RichIterable<DirectiveUse> d1,
		RichIterable<DirectiveUse> d2
	)
	{
		MutableMap<String, DirectiveUse> directives = Maps.mutable.empty();
		for(DirectiveUse d : d1)
		{
			directives.put(d.getName(), d);
		}

		for(DirectiveUse d : d2)
		{
			DirectiveUse current = directives.get(d.getName());
			if(current == null)
			{
				directives.put(d.getName(), d);
			}
			else
			{
				directives.put(d.getName(), mergeDirective(current, d));
			}
		}

		return directives.toList().toImmutable();
	}

	private DirectiveUse mergeDirective(DirectiveUse d1, DirectiveUse d2)
	{
		// TODO: Logic for merging the directives
		return d1;
	}

	private ImmutableList<FieldDef> mergeFields(
		RichIterable<FieldDef> f1,
		RichIterable<FieldDef> f2
	)
	{
		MutableMap<String, FieldDef> fields = Maps.mutable.empty();
		for(FieldDef field : f1)
		{
			fields.put(field.getName(), field);
		}

		for(FieldDef field : f2)
		{
			FieldDef current = fields.get(field.getName());
			if(current == null)
			{
				fields.put(field.getName(), field);
			}
			else
			{
				fields.put(field.getName(), mergeField(current, field));
			}
		}

		return fields.toList().toImmutable();
	}

	private FieldDef mergeField(FieldDef d1, FieldDef d2)
	{
		// TODO: Logic for merging the fields
		return d1;
	}

	/**
	 * Get a description of where the given type has been defined.
	 *
	 * @param current
	 * @return
	 */
	private String toLocation(TypeDef current)
	{
		if(current instanceof HasSourceLocation)
		{
			return ((HasSourceLocation) current).getSourceLocation().toString();
		}
		else
		{
			return "Unknown Location";
		}
	}
}
