package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.mutation.TypedMutation;
import com.circumgraph.storage.types.ValueValidator;

import org.eclipse.collections.api.map.MapIterable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper that can handle polymorphism of types. Supports storing either as
 * {@link StructuredValue} or {@link StoredObjectRef}. Mutation is done via
 * {@link StructuredMutation} or {@link StoredObjectRefMutation}.
 */
public class PolymorphicValueMapper
	implements ValueMapper<Value, TypedMutation>
{
	private final OutputTypeDef type;
	private final MapIterable<String, ValueMapper<?, ?>> subTypes;
	private final ValueValidator<Value> validator;

	public PolymorphicValueMapper(
		OutputTypeDef type,
		MapIterable<String, ValueMapper<?, ?>> subTypes,
		ValueValidator<Value> validator
	)
	{
		this.type = type;
		this.subTypes = subTypes;
		this.validator = validator;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return type;
	}

	@Override
	public Mono<Value> getInitialValue()
	{
		// TODO: Initial value should be supported by polymorphic types
		return Mono.empty();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mono<Value> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		Value previousValue,
		TypedMutation mutation
	)
	{
		return Mono.defer(() -> {
			var mapper = subTypes.get(mutation.getDef().getName());
			if(mapper == null)
			{
				/*
				 * Tried to store an unsupported type, report an error and
				 * return nothing.
				 */
				encounter.reportError(createInvalidSubTypeError(location, mutation.getDef()));

				return Mono.empty();
			}

			return ((ValueMapper) mapper).applyMutation(encounter, location, previousValue, mutation);
		});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		Value value
	)
	{
		if(value == null)
		{
			// Null values are not validated
			return Flux.empty();
		}

		var mapper = subTypes.get(value.getDefinition().getName());
		if(mapper == null)
		{
			/*
			 * Type is invalid, report single error.
			 */
			return Flux.just(createInvalidSubTypeError(location, value.getDefinition()));
		}
		else
		{
			return ((ValueMapper) mapper).validate(location, value)
				.thenMany(validator.validate(location, value));
		}
	}

	private ValidationMessage createInvalidSubTypeError(
		ObjectLocation location,
		TypeDef receivedType
	)
	{
		return ValidationMessage.error()
			.withLocation(location)
			.withMessage("Unable to mutate, expected sub-type of %s but got %s", type.getName(), receivedType.getName())
			.withCode("storage:mutation:invalid-type")
			.withArgument("expectedType", type.getName())
			.withArgument("givenType", receivedType.getName())
			.build();
	}
}
