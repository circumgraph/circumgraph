package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.storage.Value;

import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import se.l4.silo.Transaction;

public class NonNullOutputMapper<I extends Value>
	implements OutputMapper<I>
{
	private final GraphQLNonNull type;
	private final OutputMapper<I> mapper;

	public NonNullOutputMapper(
		OutputMapper<I> mapper
	)
	{
		this.type = GraphQLNonNull.nonNull(mapper.getGraphQLType());
		this.mapper = mapper;
	}

	@Override
	public GraphQLOutputType getGraphQLType()
	{
		return type;
	}

	@Override
	public Object toOutput(Transaction tx, I in)
	{
		return mapper.toOutput(tx, in);
	}
}
