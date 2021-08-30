package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.types.ValueMapper;

import reactor.core.publisher.Mono;

/**
 * Mapper for scalar values.
 */
public class ScalarValueMapper
	implements ValueMapper<SimpleValue, ScalarValueMutation<?>>
{
	private final ScalarDef typeDef;

	public ScalarValueMapper(
		ScalarDef typeDef
	)
	{
		this.typeDef = typeDef;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return typeDef;
	}

	@Override
	public Mono<SimpleValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		SimpleValue previousValue,
		ScalarValueMutation<?> mutation
	)
	{
		return Mono.just(SimpleValue.create(typeDef, mutation.getValue()));
	}

	@Override
	public String toString()
	{
		return "ScalarValueMapper{typeDef=" + typeDef + "}";
	}
}
