package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.model.ListDef;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.Value;

import org.eclipse.collections.api.block.predicate.Predicate;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.l4.silo.Transaction;

/**
 * {@link OutputMapper} for {@link ListValue}.
 */
public class ListOutputMapper<I extends Value, O>
	implements OutputMapper<ListValue<I>>
{
	private final GraphQLOutputType type;
	private final OutputMapper<I> itemMapper;

	public ListOutputMapper(
		ListDef.Output type,
		OutputMapper<I> itemMapper
	)
	{
		this.itemMapper = itemMapper;
		this.type = GraphQLList.list(itemMapper.getGraphQLType());
	}

	@Override
	public GraphQLOutputType getGraphQLType()
	{
		return type;
	}

	@Override
	public Object toOutput(Transaction tx, ListValue<I> in)
	{

		return Mono.fromSupplier(() -> {
			var items = in.items()
				.collect(item -> itemMapper.toOutput(tx, item));

			if(items.anySatisfy(HAS_MONO_OR_FLUX))
			{
				return items
					.collect(item -> {
						if(item instanceof Mono<?> m)
						{
							return m.block();
						}
						else if(item instanceof Flux<?> f)
						{
							return f.toIterable();
						}
						else
						{
							return item;
						}
					});
			}
			else
			{
				return items;
			}
		});
	}

	private static final Predicate<Object> HAS_MONO_OR_FLUX = o -> o instanceof Mono || o instanceof Flux;
}
