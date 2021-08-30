package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.ValueMappers;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.types.ValueMapper;

import reactor.core.publisher.Mono;

public class DeferredValueMapper
	implements ValueMapper<Value, Mutation>
{
	private final HasMetadata def;

	private ValueMapper mapper;

	public DeferredValueMapper(HasMetadata def)
	{
		this.def = def;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return (OutputTypeDef) def;
	}

	@SuppressWarnings("rawtypes")
	private ValueMapper mapper()
	{
		var mapper = this.mapper;
		if(mapper == null)
		{
			mapper = def.getMetadata(ValueMappers.MAPPER).get();
			this.mapper = mapper;
		}

		return mapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Mono<Value> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		Value previousValue,
		Mutation mutation
	)
	{
		return mapper().applyMutation(encounter, location, previousValue, mutation);
	}

	@Override
	public String toString()
	{
		return "DeferredValueMapper{def=" + def + "}";
	}
}
