package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.mutation.ListMutation;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.Value;

import org.eclipse.collections.impl.collector.Collectors2;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ListValueMapper<V extends Value, M extends Mutation>
	implements ValueMapper<ListValue<V>, ListMutation<M>>
{
	private static final Object NULL = new Object();

	private final ListDef.Output typeDef;

	private final ValueValidator<ListValue<V>> validator;
	private final ValueMapper<V, M> itemMapper;

	public ListValueMapper(
		ListDef.Output typeDef,
		ValueValidator<ListValue<V>> validator,
		ValueMapper<V, M> itemMapper
	)
	{
		this.typeDef = typeDef;
		this.validator = validator;
		this.itemMapper = itemMapper;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return typeDef;
	}

	@Override
	public Mono<ListValue<V>> getInitialValue()
	{
		return Mono.empty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Mono<ListValue<V>> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		ListValue<V> previousValue,
		ListMutation<M> mutation
	)
	{
		return Mono.defer(() -> {
			if(mutation instanceof ListSetMutation)
			{
				/*
				 * Set mutations fully replace the previous value, map the
				 * new values and return them.
				 */
				var casted = (ListSetMutation<M>) mutation;
				return Flux.fromIterable(casted.getValues())
					.flatMapSequential(m -> {
						if(m instanceof NullMutation)
						{
							/*
							* NullMutation should set the value to null - validate
							* that is possible before attempting to do so.
							*/
							return itemMapper.validate(location, null)
								.doOnNext(encounter::reportError)
								.then(Mono.just(NULL));
						}

						return itemMapper.applyMutation(
							encounter,
							location,
							null,
							m
						);
					})
					.collect(Collectors2.toImmutableList())
					.flatMap(values -> {
						var value = ListValue.create(
							typeDef,
							values.collect(v -> v == NULL ? null : (V) v)
						);

						return validator.validate(location, value)
							.doOnNext(encounter::reportError)
							.then(Mono.just(value));
					});
			}

			return Mono.just(previousValue);
		});
	}

	@Override
	public Flux<ValidationMessage> validate(
		ObjectLocation location,
		ListValue<V> value
	)
	{
		if(value == null)
		{
			return validator.validate(location, value);
		}

		return Flux.fromIterable(value.items())
			.flatMap(v -> itemMapper.validate(location, v))
			.thenMany(validator.validate(location, value));
	}
}
