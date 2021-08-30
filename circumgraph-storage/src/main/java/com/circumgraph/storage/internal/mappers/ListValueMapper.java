package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.ValueMutationHandler;
import com.circumgraph.storage.mutation.ListMutation;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.types.ValueMapper;

import org.eclipse.collections.impl.collector.Collectors2;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ListValueMapper<V extends Value, M extends Mutation>
	implements ValueMapper<ListValue<V>, ListMutation<M>>
{
	private static final Object NULL = new Object();

	private final ListDef.Output typeDef;
	private final ValueMutationHandler<V, M> itemMapper;

	public ListValueMapper(
		ListDef.Output typeDef,
		ValueMutationHandler<V, M> itemMapper
	)
	{
		this.typeDef = typeDef;
		this.itemMapper = itemMapper;
	}

	@Override
	public OutputTypeDef getDef()
	{
		return typeDef;
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
							return itemMapper.getValidator().validate(location, null)
								.doOnNext(encounter::reportError)
								.then(Mono.just(NULL));
						}

						return itemMapper.getMapper().applyMutation(
							encounter,
							location,
							null,
							m
						);
					})
					.collect(Collectors2.toImmutableList())
					.map(values -> ListValue.create(
							typeDef,
							values.collect(v -> v == NULL ? null : (V) v)
						)
					);
			}

			return Mono.just(previousValue);
		});
	}
}
