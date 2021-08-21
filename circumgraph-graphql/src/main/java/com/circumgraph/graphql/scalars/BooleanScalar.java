package com.circumgraph.graphql.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

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
}

