package com.circumgraph.graphql.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.graphql.internal.StorageIds;
import com.circumgraph.model.ScalarDef;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

public class IDScalar
	implements ScalarMapper<Long>
{
	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.ID;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLID;
	}

	@Override
	public Long fromInput(Object inputValue)
	{
		return StorageIds.decode(inputValue.toString());
	}
}
