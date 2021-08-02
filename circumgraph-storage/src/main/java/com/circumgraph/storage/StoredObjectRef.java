package com.circumgraph.storage;

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
