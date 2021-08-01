package com.circumgraph.storage.search;

import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.search.QueryImpl;

import se.l4.silo.index.FieldSort;
import se.l4.silo.index.search.QueryClause;
import se.l4.silo.index.search.query.FieldQuery;

/**
 * Query for a {@link StoredObjectValue} in a {@link com.circumgraph.storage.Collection}.
 */
public interface Query
{
	/**
	 * Create an empty query.
	 *
	 * @return
	 */
	static Query create()
	{
		return new QueryImpl();
	}

	/**
	 * Set the page to return. Pagination for queries use a
	 * {@link Page#getLimit() limit} for the number of results to return, with
	 * an optional {@link Page#getCursor()} defining from where pagination
	 * should occur.
	 *
	 * @param page
	 *   page, never {@code null}
	 * @return
	 *   self
	 */
	Query withPage(Page page);

	/**
	 * Set if scores need to be calculated. This will default to {@code false}
	 * if this is not used.
	 *
	 * No scores means that all {@link Edge edges} will return {@code 0} instead
	 * of the actual score when {@link Edge#getScore()} is called. This allows
	 * for an optimization in the query where it can be resolved faster.
	 *
	 * @param scoresNeeded
	 *   {@code true} if scores should be calculated
	 * @return
	 *   self
	 */
	Query withScoresNeeded(boolean scoresNeeded);

	/**
	 * Add a clause to the query. All top level clauses must match, effectively
	 * creating a {@link se.l4.silo.index.search.query.AndQuery}.
	 *
	 * {@link FieldQuery} can be used to match a field in combination with
	 * a {@link se.l4.silo.index.Matcher}. Different types will support
	 * different matchers depending on how they were indexed.
	 *
	 * @param clause
	 *   clause that must match
	 * @return
	 *   self
	 */
	Query addClause(QueryClause clause);

	/**
	 * Add several clauses to the query. See {@link #addClause(QueryClause)}
	 * for details on clauses.
	 *
	 * @param clauses
	 * @return
	 */
	Query addClauses(Iterable<? extends QueryClause> clauses);

	/**
	 * Add a sort on the given field.
	 *
	 * @param field
	 *   the field to sort on
	 * @param ascending
	 *   if the sort should be ascending
	 * @return
	 *   self
	 */
	Query addSort(String field, boolean ascending);

	/**
	 * Add a pre-built sort.
	 *
	 * @param sort
	 *   instance of {@link FieldSort}
	 * @return
	 *   self
	 */
	Query addSort(FieldSort sort);

	/**
	 * Add several pre-built sorts.
	 *
	 * @param sorts
	 *   iterable containing all sorts
	 * @return
	 *   self
	 */
	Query addSort(Iterable<? extends FieldSort> sorts);
}
