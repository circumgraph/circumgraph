package com.circumgraph.graphql.internal.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.SimpleValue;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

public class StringScalar
	implements ScalarMapper<String>
{
	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.STRING;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLString;
	}

	@Override
	public String fromInput(Object inputValue)
	{
		return inputValue.toString();
	}

	@Override
	public Object toOutput(SimpleValue value)
	{
		return value == null ? null : value.get();
	}
}
