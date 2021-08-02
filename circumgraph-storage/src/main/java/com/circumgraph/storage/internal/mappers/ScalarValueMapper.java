package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper for scalar values.
 */
public class ScalarValueMapper
	implements ValueMapper<SimpleValue, ScalarValueMutation<?>>
{
	private final ScalarDef typeDef;
	private final ValueProvider<SimpleValue> defaultValue;
	private final ValueValidator<SimpleValue> validator;

	public ScalarValueMapper(
		ScalarDef typeDef,
		ValueProvider<SimpleValue> defaultValue,
		ValueValidator<SimpleValue> validator
	)
	{
		this.typeDef = typeDef;
		this.defaultValue = defaultValue;
		this.validator = validator;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return typeDef;
	}

	@Override
	public Mono<SimpleValue> getInitialValue()
	{
		return defaultValue == null
			? Mono.empty()
			: defaultValue.create();
	}

	@Override
	public Mono<SimpleValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		SimpleValue previousValue,
		ScalarValueMutation<?> mutation
	)
	{
		return Mono.defer(() -> {
			var value = SimpleValue.create(typeDef, mutation.getValue());

			return validate(location, value)
				.doOnNext(encounter::reportError)
				.then(Mono.just(value));
		});
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		SimpleValue value
	)
	{
		return validator.validate(location, value);
	}
}
