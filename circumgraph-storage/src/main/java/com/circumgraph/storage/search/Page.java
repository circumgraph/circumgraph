package com.circumgraph.storage.search;

import java.util.Optional;

import com.circumgraph.storage.internal.search.PageImpl;

/**
 * Information about page being queried.
 */
public interface Page
{
	/**
	 * Get the number of items to return.
	 *
	 * @return
	 */
	int getLimit();

	/**
	 * Get the cursor for the position.
	 *
	 * @return
	 */
	Optional<Cursor> getCursor();

	/**
	 * Get a page representing the given number of results.
	 *
	 * @param limit
	 * @return
	 */
	static Page first(int limit)
	{
		return new PageImpl(limit, null);
	}

	/**
	 * Get a page representing the given number of results after a specific
	 * cursor.
	 */
	static Page first(int limit, Cursor after)
	{
		return new PageImpl(limit, after);
	}
}
