package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.StoredObjectValueImpl;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.values.StructuredValue;

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
	public StoredObjectValue getInitialValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredObjectValue applyMutation(
		StoredObjectValue previousValue,
		StructuredMutation mutation
	)
	{
		var mutated = mapper.applyMutation(previousValue, mutation);
		return new StoredObjectValueImpl(mutated);
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		StoredObjectValue value
	)
	{
		mapper.validate(validationCollector, value);
	}
}
