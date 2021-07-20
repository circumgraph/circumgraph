package com.circumgraph.storage.internal;

import java.util.Optional;

import com.circumgraph.storage.types.ValueProvider;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

import se.l4.ylem.ids.SimpleLongIdGenerator;

/**
 * Service to keep track of the available {@link ValueProvider} instances.
 */
public class ValueProviders
{
	private final ImmutableMap<String, ValueProvider<?>> instances;

	public ValueProviders()
	{
		instances = Maps.immutable.of(
			"ID", new GeneratedIdValueProvider(new SimpleLongIdGenerator())
		);
	}

	public Optional<ValueProvider<?>> get(String id)
	{
		return Optional.ofNullable(instances.get(id));
	}
}
