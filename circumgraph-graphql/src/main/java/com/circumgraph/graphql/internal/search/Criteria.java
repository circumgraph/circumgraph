package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.storage.search.QueryPath;

import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.search.QueryClause;

public interface Criteria
{
	/**
	 * Get the GraphQL type for the criteria.
	 *
	 * @return
	 */
	GraphQLInputObjectType getGraphQLType();

	QueryClause toClause(
		Map<String, Object> data,
		QueryPath path
	);
}
