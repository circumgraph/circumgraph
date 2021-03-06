package com.circumgraph.storage.search;

import com.circumgraph.storage.StoredEntityValue;

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
	 * Generate cursors for pagination of this result.
	 *
	 * @param max
	 *   the maximum number of pages, including separators
	 * @return
	 */
	PageCursors getPageCursors(int max);

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
	ListIterable<StoredEntityValue> getNodes();
}
