package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.internal.StoredEntityValueImpl;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.values.StructuredValue;

/**
 * Mapper for root object stored in an entity.
 */
public class EntityMapper
	implements ValueMapper<StoredEntityValue, StructuredMutation>
{
	private final ValueMapper<StructuredValue, StructuredMutation> mapper;


	public EntityMapper(
		ValueMapper<StructuredValue, StructuredMutation> mapper
	)
	{
		this.mapper = mapper;
	}

	@Override
	public StoredEntityValue getInitialValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredEntityValue applyMutation(
		StoredEntityValue previousValue,
		StructuredMutation mutation
	)
	{
		var mutated = mapper.applyMutation(previousValue, mutation);
		return new StoredEntityValueImpl(mutated);
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		StoredEntityValue value
	)
	{
		mapper.validate(validationCollector, value);
	}
}
