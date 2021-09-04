package com.circumgraph.graphql.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

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
}

