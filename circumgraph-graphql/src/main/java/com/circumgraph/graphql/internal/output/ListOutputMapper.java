package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.model.ListDef;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.Value;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;

/**
 * {@link OutputMapper} for {@link ListValue}.
 */
public class ListOutputMapper<I extends Value, O>
	implements OutputMapper<ListValue<I>, Iterable<O>>
{
	private final GraphQLOutputType type;
	private final OutputMapper<I, O> itemMapper;

	public ListOutputMapper(
		ListDef.Output type,
		OutputMapper<I, O> itemMapper
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
	public Iterable<O> toOutput(ListValue<I> in)
	{
		return in.items().collect(item -> itemMapper.toOutput(item));
	}
}
