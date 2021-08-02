package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueValidator;

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
	private final MapIterable<String, ValueMapper<?, ?>> fields;
	private final ValueValidator<StructuredValue> validator;

	public StructuredValueMapper(
		StructuredDef type,
		MapIterable<String, ValueMapper<?, ?>> fields,
		ValueValidator<StructuredValue> validator
	)
	{
		this.type = type;
		this.fields = fields;
		this.validator = validator;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return type;
	}

	@Override
	public Mono<StructuredValue> getInitialValue()
	{
		return Flux.fromIterable(fields.keyValuesView())
			.flatMap(pair -> {
				var key = pair.getOne();
				var mapper = pair.getTwo();

				return mapper.getInitialValue()
					.map(initialValue -> Tuples.pair(key, initialValue));
			})
			.collect(Collectors2.toMap(Pair::getOne, Pair::getTwo))
			.map(newValues -> StructuredValue.create(type, newValues));
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
					var mapper = (ValueMapper<Value, Mutation>) pair.getTwo();

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
							if(previousFieldValue.getDefinition() == mapper.getDef())
							{
								// TODO: This should check if type are compatible - not equal, type widening and narrowing should be supportd
								return Mono.just(Tuples.pair(key, previousFieldValue));
							}
						}

						// No value, create and validate initial value
						return mapper.getInitialValue()
							.flatMap(initialValue -> {
								return mapper.validate(
									location.forField(key),
									initialValue
								)
									.doOnNext(encounter::reportError)
									.then(Mono.just(Tuples.pair(key, initialValue)));
							})
							// If there is no value - validate it as null
							.switchIfEmpty(mapper.validate(location.forField(key), null)
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
						return mapper.validate(location, null)
							.doOnNext(encounter::reportError)
							.then(Mono.empty());
					}

					/*
					 * Field is being mutated so defer to the mapper for
					 * the field.
					 */
					return mapper.applyMutation(
						encounter,
						location.forField(key),
						previousFieldValues.get(key),
						fieldMutation
					).map(updatedValue -> Tuples.pair(key, updatedValue));
				})
				.collect(Collectors2.toMap(Pair::getOne, Pair::getTwo))
				.map(newValues -> StructuredValue.create(type, newValues))
				.flatMap(newValue -> {
					/*
					 * There may be some validation that applies to the
					 * entire structured value. So validate after mutation has
					 * been applied.
					 */
					return validator.validate(location, newValue)
						.doOnNext(encounter::reportError)
						.then(Mono.just(newValue));
				});
		});
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		StructuredValue value
	)
	{
		var values = value.getFields();
		return Flux.fromIterable(this.fields.keyValuesView())
			.flatMap(p -> {
				var key = p.getOne();
				var fieldValue = values.get(key);
				var fieldMapper = (ValueMapper<Value, Mutation>) p.getTwo();

				return fieldMapper.validate(location.forField(key), fieldValue);
			})
			.thenMany(validator.validate(location, value));
	}
}
