package com.circumgraph.storage.types;

import com.circumgraph.model.ScalarDef;

/**
 * Interface used to provide indexing support for scalars.
 */
public interface ValueIndexer<V>
{
	/**
	 * Get the type this indexes.
	 *
	 * @return
	 */
	ScalarDef getScalarType();
}
