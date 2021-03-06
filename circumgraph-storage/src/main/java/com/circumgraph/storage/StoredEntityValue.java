package com.circumgraph.storage;

import com.circumgraph.values.StructuredValue;

/**
 * Extension to {@link StructuredValue} that represents a stored entity. Used
 * as a thin layer to provide access to {@link #getId() ids} and to support
 * automatic loading of references.
 */
public interface StoredEntityValue
	extends StructuredValue
{
	/**
	 * Get the identifier of the stored entity.
	 *
	 * @return
	 */
	long getId();
}
