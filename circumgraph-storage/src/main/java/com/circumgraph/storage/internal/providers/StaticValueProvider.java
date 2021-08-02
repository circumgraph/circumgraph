package com.circumgraph.storage.internal.providers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueProvider;

import reactor.core.publisher.Mono;

/**
 * Implementation of {@link ValueProvider} that returns a static value.
 */
public class StaticValueProvider<V extends Value>
	implements ValueProvider<V>
{
	private final V value;

	public StaticValueProvider(
		V value
	)
	{
		this.value = value;
	}

	@Override
	public OutputTypeDef getType()
	{
		return (OutputTypeDef) value.getDefinition();
	}

	@Override
	public Mono<V> create()
	{
		return Mono.just(value);
	}
}
