package com.circumgraph.graphql;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import graphql.schema.GraphQLScalarType;

/**
 * Mapper for scalars defined via {@link ScalarDef}.
 */
public interface ScalarMapper<V>
{
	/**
	 * Get how this scalar is defined in the model.
	 *
	 * @return
	 */
	ScalarDef getModelDef();

	/**
	 * Get the type used in the GraphQL schema.
	 *
	 * @return
	 */
	GraphQLScalarType getGraphQLType();
}
