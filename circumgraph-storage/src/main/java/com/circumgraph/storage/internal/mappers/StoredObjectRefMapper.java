package com.circumgraph.storage.internal.mappers;

import java.util.function.Supplier;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.internal.StoredObjectRefImpl;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StoredObjectRefMapper
	implements ValueMapper<StoredObjectRef, StoredObjectRefMutation>
{
	private static final ValidationMessageType INVALID_REFERENCE = ValidationMessageType.error()
		.withCode("storage:mutation:invalid-reference")
		.withMessage("Invalid reference to {{type}}, object with given {{id}} does not exist")
		.withArgument("type")
		.withArgument("id")
		.build();

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
	public OutputTypeDef getDef()
	{
		return def;
	}

	@Override
	public Mono<StoredObjectRef> getInitialValue()
	{
		return Mono.empty();
	}

	@Override
	public Mono<StoredObjectRef> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		StoredObjectRef previousValue,
		StoredObjectRefMutation mutation
	)
	{
		return Mono.defer(() -> {
			var value = new StoredObjectRefImpl(def, mutation.getId());

			return validate(location, value)
				.doOnNext(encounter::reportError)
				.then(Mono.just(value));
		});
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		StoredObjectRef value
	)
	{
		return collection.get()
			.contains(value.getId())
			.flatMapMany(b -> {
				if(b)
				{
					return Flux.empty();
				}
				else
				{
					return Flux.just(INVALID_REFERENCE.toMessage()
						.withLocation(location)
						.withArgument("type", def.getName())
						.withArgument("id", value.getId())
						.build()
					);
				}
			});
	}
}
