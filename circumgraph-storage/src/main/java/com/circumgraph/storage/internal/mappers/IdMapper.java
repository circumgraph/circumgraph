package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IdMapper
	implements ValueMapper<SimpleValue, ScalarValueMutation<?>>
{
	@Override
	public OutputTypeDef getDef()
	{
		return ScalarDef.ID;
	}

	@Override
	public Mono<SimpleValue> getInitialValue()
	{
		return Mono.empty();
	}

	@Override
	public Mono<SimpleValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		SimpleValue previousValue,
		ScalarValueMutation<?> mutation
	)
	{
		return Mono.just(previousValue);
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		SimpleValue value
	)
	{
		return Flux.empty();
	}
}
