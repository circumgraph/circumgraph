package com.circumgraph.storage.search;

import com.circumgraph.storage.StoredEntityValue;

/**
 * Hit within a {@link SearchResult}.
 */
public interface Edge
{
	/**
	 * Get the score.
	 *
	 * @return
	 */
	float getScore();

	/**
	 * Get cursor for edge.
	 *
	 * @return
	 */
	Cursor getCursor();

	/**
	 * Get the node.
	 *
	 * @return
	 */
	StoredEntityValue getNode();
}
