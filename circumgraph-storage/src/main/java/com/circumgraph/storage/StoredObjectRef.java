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
	String getId();

	/**
	 * Get the decoded identifier.
	 *
	 * @return
	 */
	long getDecodedId();
}
