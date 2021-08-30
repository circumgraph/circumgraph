package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.ValueMutationHandler;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueMapper;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.tuple.Tuples;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper for {@link StructuredValue}.
 */
public class StructuredValueMapper
	implements ValueMapper<StructuredValue, StructuredMutation>
{
	private final StructuredDef type;
	private final MapIterable<String, ValueMutationHandler<?, ?>> fields;

	public StructuredValueMapper(
		StructuredDef type,
		MapIterable<String, ValueMutationHandler<?, ?>> fields
	)
	{
		this.type = type;
		this.fields = fields;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return type;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public Mono<StructuredValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		StructuredValue previousValue,
		StructuredMutation mutation
	)
	{
		return Mono.defer(() -> {
			boolean hasPreviousValue = previousValue != null;

			MutableMap<String, Value> previousFieldValues = hasPreviousValue
				? Maps.mutable.ofMapIterable(previousValue.getFields())
				: Maps.mutable.ofInitialCapacity(fields.size());

			var fieldMutations = mutation.getFields();

			return Flux.fromIterable(fields.keyValuesView())
				.flatMap(pair -> {
					var key = pair.getOne();
					var mapper = (ValueMutationHandler<Value, Mutation>) pair.getTwo();

					var fieldMutation = fieldMutations.get(key);
					if(fieldMutation == null)
					{
						/*
						 * Not mutating the value means two cases:
						 *
						 * 1) A value exists, return it
						 * 2) No value exists, create a default value
						 */
						if(previousFieldValues.containsKey(key))
						{
							/*
							 * This value has previously been set, return it if
							 * the definition is compatible.
							 */
							var previousFieldValue = previousFieldValues.get(key);
							if(mapper.getDef().isAssignableFrom(previousFieldValue.getDefinition()))
							{
								// TODO: This should check if type are compatible - not equal, type widening and narrowing should be supportd
								return Mono.just(Tuples.pair(key, previousFieldValue));
							}
						}

						// No value, create and validate initial value
						return mapper.getDefault().create()
							.flatMap(initialValue -> {
								return mapper.getValidator().validate(
									location.forField(key),
									initialValue
								)
									.doOnNext(encounter::reportError)
									.then(Mono.just(Tuples.pair(key, initialValue)));
							})
							// If there is no value - validate it as null
							.switchIfEmpty(mapper.getValidator().validate(location.forField(key), null)
								.doOnNext(encounter::reportError)
								.then(Mono.empty())
							);
					}

					if(fieldMutation instanceof NullMutation)
					{
						/*
						 * NullMutation should set the value to null - validate
						 * that is possible before attempting to do so.
						 */
						return mapper.getValidator()
							.validate(location.forField(key), null)
							.doOnNext(encounter::reportError)
							.then(Mono.empty());
					}

					/*
					 * Field is being mutated so defer to the mapper for
					 * the field.
					 */
					return mapper.getMapper().applyMutation(
						encounter,
						location.forField(key),
						previousFieldValues.get(key),
						fieldMutation
					).map(updatedValue -> Tuples.pair(key, updatedValue));
				})
				.collect(Collectors2.toMap(Pair::getOne, Pair::getTwo))
				.map(newValues -> StructuredValue.create(type, newValues));
		});
	}
}
