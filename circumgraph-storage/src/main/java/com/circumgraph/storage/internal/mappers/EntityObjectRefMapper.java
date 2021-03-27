package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Entity;
import com.circumgraph.storage.EntityObjectRef;
import com.circumgraph.storage.internal.EntityObjectRefImpl;
import com.circumgraph.storage.mutation.SimpleValueMutation;

public class EntityObjectRefMapper
	implements ValueMapper<EntityObjectRef, SimpleValueMutation<Long>>
{
	private final StructuredDef def;
	private final Supplier<Entity> entity;

	public EntityObjectRefMapper(
		StructuredDef def,
		Supplier<Entity> entity
	)
	{
		this.entity = entity;
		this.def = def;
	}

	@Override
	public EntityObjectRef getInitialValue()
	{
		return null;
	}

	@Override
	public EntityObjectRef applyMutation(
		EntityObjectRef previousValue,
		SimpleValueMutation<Long> mutation
	)
	{
		return new EntityObjectRefImpl(def, mutation.getValue());
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		EntityObjectRef value
	)
	{
		entity.get()
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
