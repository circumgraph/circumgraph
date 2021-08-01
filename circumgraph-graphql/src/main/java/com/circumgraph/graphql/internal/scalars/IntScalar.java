package com.circumgraph.graphql.internal.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.SimpleValue;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

public class IntScalar
	implements ScalarMapper<Integer>
{
	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.INT;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLInt;
	}

	@Override
	public Integer fromInput(Object inputValue)
	{
		return ((Number) inputValue).intValue();
	}

	@Override
	public Object toOutput(SimpleValue value)
	{
		return value == null ? null : value.get();
	}
}