package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.storage.Value;

import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import se.l4.silo.Transaction;

public class NonNullOutputMapper<I extends Value, O>
	implements OutputMapper<I, O>
{
	private final GraphQLNonNull type;
	private final OutputMapper<I, O> mapper;

	public NonNullOutputMapper(
		OutputMapper<I, O> mapper
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
	public O toOutput(Transaction tx, I in)
	{
		return mapper.toOutput(tx, in);
	}
}
