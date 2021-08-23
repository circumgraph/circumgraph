package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.search.QueryClause;

public interface Criteria
{
	/**
	 * Get the type this represents in the model.
	 *
	 * @return
	 */
	OutputTypeDef getModelDef();

	/**
	 * Get the GraphQL type for the criteria.
	 *
	 * @return
	 */
	InputObjectDef getGraphQLType();

	/**
	 * Convert into a {@link QueryClause}.
	 *
	 * @param data
	 *   data to convert
	 * @param path
	 *   the current path to query
	 * @return
	 *   clause
	 */
	QueryClause toClause(
		Map<String, Object> data,
		QueryPath path
	);
}
