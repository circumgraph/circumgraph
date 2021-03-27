package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.internal.StoredObjectRefImpl;
import com.circumgraph.storage.mutation.SimpleValueMutation;

public class StoredObjectRefMapper
	implements ValueMapper<StoredObjectRef, SimpleValueMutation<Long>>
{
	private final StructuredDef def;
	private final Supplier<Collection> collection;

	public StoredObjectRefMapper(
		StructuredDef def,
		Supplier<Collection> collection
	)
	{
		this.collection = collection;
		this.def = def;
	}

	@Override
	public StoredObjectRef getInitialValue()
	{
		return null;
	}

	@Override
	public StoredObjectRef applyMutation(
		StoredObjectRef previousValue,
		SimpleValueMutation<Long> mutation
	)
	{
		return new StoredObjectRefImpl(def, mutation.getValue());
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		StoredObjectRef value
	)
	{
		collection.get()
			.contains(value.getId())
			.doOnNext(exists -> {
				if(! exists)
				{
					validationCollector.accept(ValidationMessage.error()
						.withCode("storage:invalid-reference")
						.withMessage("Invalid reference to `%s`, object with id `%s` does not exist", def.getName(), value.getId())
						.withArgument("type", def.getName())
						.withArgument("id", value.getId())
						.build()
					);
				}
			})
			.block();
	}
}
