package com.circumgraph.model;

import com.circumgraph.model.internal.ObjectDefImpl;

/**
 * Representation of an object.
 */
public interface ObjectDef
	extends StructuredDef
{
	/**
	 * Start building a new instance of {@link ObjectDef}.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return ObjectDefImpl.create(name);
	}

	/**
	 * Builder for instances of {@link ObjectDef}.
	 */
	interface Builder
		extends StructuredDef.Builder<Builder>
	{
		/**
		 * Build the instance.
		 *
		 * @return
		 */
		ObjectDef build();
	}
}
