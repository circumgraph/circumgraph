package com.circumgraph.storage.search;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Information about pagination.
 */
public interface PageCursors
{
	/**
	 * Get a cursor to the previous page.
	 *
	 * @return
	 */
	PageCursor getPrevious();

	/**
	 * Get cursors representing the start of pagination.
	 *
	 * @return
	 */
	ListIterable<? extends PageCursor> getStart();

	/**
	 * Get cursors representing the middle of pagination.
	 *
	 * @return
	 */
	ListIterable<? extends PageCursor> getMiddle();

	/**
	 * Get cursors representing the end of pagination.
	 *
	 * @return
	 */
	ListIterable<? extends PageCursor> getEnd();

	/**
	 * Get a cursor to the next page.
	 *
	 * @return
	 */
	PageCursor getNext();
}
