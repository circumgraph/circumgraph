package com.circumgraph.storage.search;

import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Result of a search.
 */
public interface SearchResult
{
	/**
	 * Get the total number of results matching this query.
	 *
	 * @return
	 */
	int getTotalCount();

	/**
	 * Get if the total count represents an estimation.
	 *
	 * @return
	 */
	boolean isTotalCountEstimated();

	/**
	 * Get information about the current page.
	 *
	 * @return
	 */
	PageInfo getPageInfo();

	/**
	 * Get the edges the result returned.
	 *
	 * @return
	 */
	ListIterable<Edge> getEdges();

	/**
	 * Get the nodes of the result.
	 *
	 * @return
	 */
	ListIterable<StructuredValue> getNodes();
}
