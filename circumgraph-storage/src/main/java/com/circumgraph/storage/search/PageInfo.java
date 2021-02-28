package com.circumgraph.storage.search;

/**
 * Information about the current page.
 */
public interface PageInfo
{
	/**
	 * Get if this result has another page.
	 *
	 * @return
	 */
	boolean hasNextPage();

	/**
	 * Get if this result has a previous page.
	 *
	 * @return
	 */
	boolean hasPreviousPage();

	/**
	 * Cursor representing the start of the results.
	 *
	 * @return
	 */
	Cursor getStartCursor();

	/**
	 * Cursor representing the end of the results.
	 *
	 * @return
	 */
	Cursor getEndCursor();
}
