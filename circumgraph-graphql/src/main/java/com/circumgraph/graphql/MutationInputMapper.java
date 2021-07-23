package com.circumgraph.graphql;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;

import graphql.schema.GraphQLInputType;

/**
 * Handler for different types of input.
 */
public interface MutationInputMapper<V>
{
	/**
	 * Get how this was declared in the model.
	 *
	 * @return
	 */
	OutputTypeDef getModelDef();

	/**
	 * Get the type of input this handles.
	 *
	 * @return
	 */
	GraphQLInputType getGraphQLType();

	/**
	 * Convert the given value into a {@link Mutation}.
	 *
	 * @param value
	 * @return
	 */
	Mutation toMutation(V value);
}
