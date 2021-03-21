package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;

/**
 * Implementation of {@link Model}.
 */
public class ModelImpl
	implements Model
{
	private final ImmutableMap<String, TypeDef> types;
	private final ImmutableSet<TypeDef> typeSet;

	public ModelImpl(
		ImmutableMap<String, TypeDef> types
	)
	{
		this.types = types;
		typeSet = Sets.immutable.ofAll(types);
	}

	@Override
	public Optional<TypeDef> get(String id)
	{
		return Optional.ofNullable(types.get(id));
	}

	@Override
	public SetIterable<? extends TypeDef> getTypes()
	{
		return typeSet;
	}

	@Override
	public SetIterable<? extends StructuredDef> getImplements(String id)
	{
		return typeSet.asLazy()
			.selectInstancesOf(StructuredDef.class)
			.select(type -> type.getImplementsNames().anySatisfy(o -> id.equals(o)))
			.toSet();
	}

	@Override
	public SetIterable<? extends StructuredDef> findImplements(String id)
	{
		var result = Sets.mutable.<StructuredDef>empty();

		for(StructuredDef def : getImplements(id))
		{
			findImplements(def, result);
		}

		return result;
	}

	private void findImplements(StructuredDef def, MutableSet<StructuredDef> result)
	{
		result.add(def);
		for(StructuredDef d : getImplements(def.getName()))
		{
			findImplements(d, result);
		}
	}
}
