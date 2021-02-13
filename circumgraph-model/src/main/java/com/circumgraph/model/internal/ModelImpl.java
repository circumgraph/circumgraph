package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;

/**
 * Implementation of {@link Model}.
 */
public class ModelImpl
	implements Model
{
	private final ImmutableMap<String, TypeDef> types;

	public ModelImpl(
		ImmutableMap<String, TypeDef> types
	)
	{
		this.types = types;
	}

	@Override
	public Optional<TypeDef> get(String id)
	{
		return Optional.ofNullable(types.get(id));
	}

	@Override
	public ListIterable<? extends TypeDef> getTypes()
	{
		return types.toList();
	}

	@Override
	public ListIterable<? extends StructuredDef> getImplements(String id)
	{
		return types.asLazy()
			.selectInstancesOf(StructuredDef.class)
			.select(type -> type.getImplementsNames().anySatisfy(o -> id.equals(o)))
			.toList();
	}
}
