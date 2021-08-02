package com.circumgraph.storage;

/**
 * Extension to {@link StructuredValue} that represents a stored object. Used
 * as a thin layer to provide access to {@link #getId() ids} in an easy way.
 */
public interface StoredObjectValue
	extends StructuredValue
{
	/**
	 * Get the identifier of the stored object.
	 *
	 * @return
	 */
	long getId();
}
