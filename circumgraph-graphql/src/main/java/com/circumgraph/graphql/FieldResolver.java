package com.circumgraph.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLOutputType;

/**
 * Resolve for the value of a field.
 */
public interface FieldResolver
{
	/**
	 * Get the arguments this field requires.
	 *
	 * @return
	 */
	Iterable<? extends GraphQLArgument> getArguments();

	/**
	 * Get the output type of the resolver.
	 *
	 * @return
	 */
	GraphQLOutputType getGraphQLType();

	/**
	 * Resolve the value.
	 *
	 * @param env
	 * @return
	 */
	Object resolve(DataFetchingEnvironment env);
}
