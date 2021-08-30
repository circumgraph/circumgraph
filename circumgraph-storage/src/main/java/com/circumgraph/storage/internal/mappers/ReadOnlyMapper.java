package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.types.ValueMapper;

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
	public String toString()
	{
		return "ReadOnlyMapper{mapper=" + mapper + "}";
	}
}
