package com.circumgraph.storage;

import com.circumgraph.values.Value;

/**
 * Reference to another stored object.
 */
public interface StoredObjectRef
	extends Value
{
	/**
	 * Get the identifier of the referenced object.
	 *
	 * @return
	 */
	long getId();
}
