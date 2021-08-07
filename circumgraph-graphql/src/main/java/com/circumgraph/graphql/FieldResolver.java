package com.circumgraph.graphql;

import graphql.schema.DataFetchingEnvironment;

/**
 * Resolve for the value of a field.
 */
@FunctionalInterface
public interface FieldResolver
{
	/**
	 * Resolve the value.
	 *
	 * @param env
	 * @return
	 */
	Object resolve(DataFetchingEnvironment env);
}
