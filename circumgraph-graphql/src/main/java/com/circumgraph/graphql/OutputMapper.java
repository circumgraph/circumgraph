package com.circumgraph.graphql;

import com.circumgraph.values.Value;

import graphql.schema.GraphQLOutputType;

/**
 * Mapper for output.
 */
public interface OutputMapper<I extends Value, O>
{
	/**
	 * Get the GraphQL type.
	 *
	 * @return
	 */
	GraphQLOutputType getGraphQLType();

	/**
	 * Convert the value into GraphQL output.
	 *
	 * @param in
	 * @return
	 */
	O toOutput(I in);
}
