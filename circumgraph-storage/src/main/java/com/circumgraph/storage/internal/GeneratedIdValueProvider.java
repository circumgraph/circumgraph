package com.circumgraph.storage.internal;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.values.SimpleValue;

import reactor.core.publisher.Mono;
import se.l4.ylem.ids.LongIdGenerator;

/**
 * Provider that generates an identifier.
 */
public class GeneratedIdValueProvider
	implements ValueProvider<SimpleValue>
{
	private final LongIdGenerator ids;

	public GeneratedIdValueProvider(LongIdGenerator ids)
	{
		this.ids = ids;
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.ID;
	}

	@Override
	public Mono<SimpleValue> create()
	{
		return Mono.just(SimpleValue.create(ScalarDef.ID, ids.next()));
	}
}
