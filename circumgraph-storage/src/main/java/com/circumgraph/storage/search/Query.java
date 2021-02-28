package com.circumgraph.storage.search;

import se.l4.silo.index.FieldSort;
import se.l4.silo.index.search.QueryClause;

public interface Query
{
	/**
	 * Set the page to return.
	 *
	 * @param page
	 * @return
	 */
	Query withPage(Page page);

	/**
	 * Get if scores need to be calculated.
	 *
	 * @param scoresNeeded
	 * @return
	 */
	Query withScoresNeeded(boolean scoresNeeded);

	/**
	 * Add a clause to the query.
	 *
	 * @param clause
	 * @return
	 */
	Query addClause(QueryClause clause);

	/**
	 * Add several clauses to the query.
	 *
	 * @param clauses
	 * @return
	 */
	Query addClauses(Iterable<? extends QueryClause> clauses);

	/**
	 * Add a sort on the given field.
	 *
	 * @param field
	 * @param ascending
	 * @return
	 */
	Query addSort(String field, boolean ascending);

	/**
	 * Add a pre-built sort.
	 *
	 * @param sort
	 * @return
	 */
	Query addSort(FieldSort sort);

	/**
	 * Add several pre-built sorts.
	 *
	 * @param sorts
	 * @return
	 */
	Query addSort(Iterable<? extends FieldSort> sorts);
}
