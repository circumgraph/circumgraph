package com.circumgraph.storage.internal.providers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueProvider;

import reactor.core.publisher.Mono;

public class EmptyValueProvider<V extends Value>
	implements ValueProvider<V>
{
	private final OutputTypeDef def;

	public EmptyValueProvider(OutputTypeDef def)
	{
		this.def = def;
	}

	@Override
	public OutputTypeDef getType()
	{
		return def;
	}

	@Override
	public Mono<V> create()
	{
		return Mono.empty();
	}
}
