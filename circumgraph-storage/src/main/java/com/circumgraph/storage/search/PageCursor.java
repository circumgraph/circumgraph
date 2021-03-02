package com.circumgraph.storage.search;

/**
 * Cursor information for a specific page.
 */
public interface PageCursor
{
	/**
	 * Get the cursor.
	 *
	 * @return
	 */
	Cursor getCursor();

	/**
	 * Get the page number.
	 *
	 * @return
	 */
	int getPageNumber();

	/**
	 * Get if this is the current page.
	 *
	 * @return
	 */
	boolean isCurrent();
}
