package com.circumgraph.graphql.internal.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.SimpleValue;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

public class FloatScalar
	implements ScalarMapper<Double>
{
	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.FLOAT;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLFloat;
	}

	@Override
	public Double fromInput(Object inputValue)
	{
		return ((Number) inputValue).doubleValue();
	}

	@Override
	public Object toOutput(SimpleValue value)
	{
		return value == null ? null : value.get();
	}
}

