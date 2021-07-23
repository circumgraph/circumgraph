package com.circumgraph.graphql.internal.mutation;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import graphql.schema.GraphQLInputType;

/**
 * Mapper used for scalars.
 */
public class ScalarMutationMapper
	implements MutationInputMapper<Object>
{
	private final ScalarMapper<?> scalar;

	public ScalarMutationMapper(
		ScalarMapper<?> scalar
	)
	{
		this.scalar = scalar;
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return scalar.getModelDef();
	}

	@Override
	public GraphQLInputType getGraphQLType()
	{
		return scalar.getGraphQLType();
	}

	@Override
	public Mutation toMutation(Object value)
	{
		return ScalarValueMutation.create(
			scalar.getModelDef(),
			scalar.fromInput(value)
		);
	}
}
