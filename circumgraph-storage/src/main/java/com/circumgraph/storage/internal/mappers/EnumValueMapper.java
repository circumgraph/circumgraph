package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.SetEnumValueMutation;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper of {@link EnumDef} to a {@link SimpleValue}.
 */
public class EnumValueMapper
	implements ValueMapper<SimpleValue, SetEnumValueMutation>
{
	private final EnumDef def;
	private final ValueProvider<SimpleValue> defaultValue;
	private final ValueValidator<SimpleValue> validator;

	public EnumValueMapper(
		EnumDef def,
		ValueProvider<SimpleValue> defaultValue,
		ValueValidator<SimpleValue> validator
	)
	{
		this.def = def;
		this.defaultValue = defaultValue;
		this.validator = validator;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return def;
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
		SetEnumValueMutation mutation
	)
	{
		return Mono.defer(() -> {
			var value = SimpleValue.create(def, mutation.getValue());

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
