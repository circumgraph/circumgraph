package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.mutation.TypedMutation;
import com.circumgraph.storage.types.ValueMapper;

import org.eclipse.collections.api.map.MapIterable;

import reactor.core.publisher.Mono;

/**
 * Mapper that can handle polymorphism of types. Supports storing either as
 * {@link StructuredValue} or {@link StoredObjectRef}. Mutation is done via
 * {@link StructuredMutation} or {@link StoredObjectRefMutation}.
 */
public class PolymorphicValueMapper
	implements ValueMapper<Value, TypedMutation>
{
	private static final ValidationMessageType TYPE_ERROR = ValidationMessageType.error()
		.withCode("storage:mutation:invalid-polymorphic-type")
		.withArgument("expectedType")
		.withArgument("givenType")
		.withMessage("Expected sub-type of {{expectedType}} but got {{givenType}}")
		.build();

	private final OutputTypeDef type;
	private final MapIterable<String, ValueMapper<?, ?>> subTypes;
	private final boolean diverging;

	public PolymorphicValueMapper(
		OutputTypeDef type,
		MapIterable<String, ValueMapper<?, ?>> subTypes,
		boolean diverging
	)
	{
		this.type = type;
		this.subTypes = subTypes;
		this.diverging = diverging;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return type;
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

			/*
			 * If this mapper supports diverging types and the new polymorphic
			 * type is not assignable from the previous type the previous value
			 * is discarded.
			 */
			var actualPreviousValue = ! diverging
				|| (previousValue != null && mapper.getDef().isAssignableFrom(previousValue.getDefinition()))
				? previousValue
				: null;

			return ((ValueMapper) mapper)
				.applyMutation(encounter, location, actualPreviousValue, mutation);
		});
	}

	private ValidationMessage createInvalidSubTypeError(
		ObjectLocation location,
		TypeDef receivedType
	)
	{
		return TYPE_ERROR.toMessage()
			.withLocation(location)
			.withArgument("expectedType", type.getName())
			.withArgument("givenType", receivedType.getName())
			.build();
	}
}
