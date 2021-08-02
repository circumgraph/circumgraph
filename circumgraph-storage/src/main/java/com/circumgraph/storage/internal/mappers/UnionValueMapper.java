package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper for unions, supports storing either as {@link StructuredValue}
 * or {@link StoredObjectRef}. Mutation is done via {@link StructuredMutation}
 * or {@link StoredObjectRefMutation}.
 */
public class UnionValueMapper
	implements ValueMapper<Value, Mutation>
{
	private final UnionDef def;

	public UnionValueMapper(
		UnionDef def
	)
	{
		this.def = def;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return def;
	}

	@Override
	public Mono<Value> getInitialValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Value> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		Value previousValue,
		Mutation mutation
	)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		Value value
	)
	{
		return null;
	}
}
