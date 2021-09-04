package com.circumgraph.graphql.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

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
}
