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
	 * Start building a new object type based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

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
		@Override
		ObjectDef build();
	}
}
