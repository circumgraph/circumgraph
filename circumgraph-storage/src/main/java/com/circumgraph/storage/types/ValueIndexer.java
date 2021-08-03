package com.circumgraph.storage.types;

import com.circumgraph.model.SimpleValueDef;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Interface used to provide indexing support for simple values. Indexers are
 * named and tied to a specific {@link SimpleValueDef}.
 */
public interface ValueIndexer
{
	/**
	 * Get the name of this indexer.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the type this indexes.
	 *
	 * @return
	 */
	SimpleValueDef getType();

	/**
	 *
	 * @return
	 */
	SearchFieldType<?> getSearchFieldType();
}
