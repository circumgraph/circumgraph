package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.internal.StoredObjectValueImpl;
import com.circumgraph.storage.mutation.StructuredMutation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper for root object stored in a {@link com.circumgraph.storage.Collection}.
 */
public class RootObjectMapper
	implements ValueMapper<StoredObjectValue, StructuredMutation>
{
	private final ValueMapper<StructuredValue, StructuredMutation> mapper;

	public RootObjectMapper(
		ValueMapper<StructuredValue, StructuredMutation> mapper
	)
	{
		this.mapper = mapper;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return mapper.getDef();
	}

	@Override
	public Mono<StoredObjectValue> getInitialValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<StoredObjectValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		StoredObjectValue previousValue,
		StructuredMutation mutation
	)
	{
		return mapper.applyMutation(
			encounter,
			location,
			previousValue,
			mutation
		).map(StoredObjectValueImpl::new);
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		StoredObjectValue value
	)
	{
		return mapper.validate(location,value);
	}
}
