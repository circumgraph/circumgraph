package com.circumgraph.graphql.internal.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.SimpleValue;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import se.l4.silo.Transaction;

public class BooleanScalar
	implements ScalarMapper<Boolean>
{
	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.BOOLEAN;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLBoolean;
	}

	@Override
	public Boolean fromInput(Object inputValue)
	{
		return (Boolean) inputValue;
	}

	@Override
	public Object toOutput(Transaction tx, SimpleValue value)
	{
		return value == null ? null : value.get();
	}
}

