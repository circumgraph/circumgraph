package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper that enforces things being read-only. Will only allow values to be
 * set once, either as an initial value or via an initial mapping.
 */
public class ReadOnlyMapper<V extends Value, M extends Mutation>
	implements ValueMapper<V, M>
{
	private static final ValidationMessageType WRITE_ERROR = ValidationMessageType.error()
		.withCode("storage:mutation:read-only")
		.withMessage("Field is read-only")
		.build();

	private final ValueMapper<V, M> mapper;

	public ReadOnlyMapper(ValueMapper<V, M> mapper)
	{
		this.mapper = mapper;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return mapper.getDef();
	}

	@Override
	public Mono<V> getInitialValue()
	{
		return mapper.getInitialValue();
	}

	@Override
	public Mono<V> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		V previousValue,
		M mutation
	)
	{
		return Mono.defer(() -> {
			if(previousValue != null)
			{
				encounter.reportError(WRITE_ERROR.toMessage()
					.withLocation(location)
					.build()
				);

				return Mono.just(previousValue);
			}

			return mapper.applyMutation(encounter, location, previousValue, mutation);
		});
	}

	@Override
	public Flux<ValidationMessage> validate(ObjectLocation location, V value)
	{
		return mapper.validate(location, value);
	}
}
