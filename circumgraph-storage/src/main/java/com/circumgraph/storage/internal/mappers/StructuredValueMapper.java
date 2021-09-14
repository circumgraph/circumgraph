package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
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
				.flatMap(pair -> map(
					encounter,
					location,
					pair.getOne(),
					(ValueMutationHandler<Value, Mutation>) pair.getTwo(),
					fieldMutations.get(pair.getOne()),
					previousFieldValues.get(pair.getOne())
				).map(updatedValue -> Tuples.pair(pair.getOne(), updatedValue)))
				.collect(Collectors2.toMap(Pair::getOne, Pair::getTwo))
				.map(newValues -> StructuredValue.create(type, newValues));
		});
	}

	private Mono<Value> map(
		MappingEncounter encounter,
		ObjectLocation location,

		String key,
		ValueMutationHandler<Value, Mutation> mutationHandler,
		Mutation fieldMutation,
		Value previousFieldValue
	)
	{
		if(fieldMutation == null)
		{
			/*
			 * Not mutating the value can result in a few cases:
			 *
			 * 1) A value exists, regeneration off, return it
			 * 2) A value exists, regeneration on, regenerate
			 * 3) No value exists, create a default value
			 */
			if(previousFieldValue != null && mutationHandler.getDef().isAssignableFrom(previousFieldValue.getDefinition()))
			{
				if(! mutationHandler.isRegenerate())
				{
					// No regeneration, simply return the previous value
					return Mono.just(previousFieldValue);
				}

				// Regeneration, let's check if we need to do something special
				if(mutationHandler.getDef() instanceof StructuredDef || mutationHandler.getDef() instanceof UnionDef)
				{
					return mutationHandler.getMapper()
						.applyMutation(
							encounter,
							location.forField(key),
							previousFieldValue,
							StructuredMutation.create((StructuredDef) previousFieldValue.getDefinition())
								.build()
						);
				}
			}

			// No value, create and validate initial value
			return mutationHandler.getDefault().create()
				.flatMap(initialValue -> {
					return mutationHandler.getValidator().validate(
						location.forField(key),
						initialValue
					)
						.doOnNext(encounter::reportError)
						.then(Mono.just(initialValue));
				})
				// If there is no value - validate it as null
				.switchIfEmpty(mutationHandler.getValidator().validate(location.forField(key), null)
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
			return mutationHandler.getValidator()
				.validate(location.forField(key), null)
				.doOnNext(encounter::reportError)
				.then(Mono.empty());
		}

		/*
		 * Field is being mutated so defer to the mapper for
		 * the field.
		 */
		return mutationHandler.getMapper()
			.applyMutation(
				encounter,
				location.forField(key),
				previousFieldValue,
				fieldMutation
			);
	}
}
