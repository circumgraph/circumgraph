package com.circumgraph.storage.internal.providers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueProvider;

import reactor.core.publisher.Mono;

/**
 * Implementation of {@link ValueProvider} that returns a static value.
 */
public class StaticValueProvider
	implements ValueProvider
{
	private final Value value;

	public StaticValueProvider(
		Value value
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
	public Mono<Value> create()
	{
		return Mono.just(value);
	}
}
