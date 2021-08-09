package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.Model;
import com.circumgraph.model.TypeDef;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
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
	@SuppressWarnings("unchecked")
	public <T extends TypeDef> Optional<T> get(String id, Class<T> type)
	{
		var value = types.get(id);
		return type.isInstance(value) ? Optional.of((T) value) : Optional.empty();
	}

	@Override
	public SetIterable<? extends TypeDef> getTypes()
	{
		return typeSet;
	}
}
