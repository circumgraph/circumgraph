package com.circumgraph.model.internal;

import com.circumgraph.model.StructuredDef;

/**
 * Interface used for things in a model that may need to be prepared for usage.
 * This is used when building a model to support things such as circular
 * resolving and caching of calculated values, such as the available fields
 * in a {@link StructuredDef}.
 */
public interface HasPreparation
{
	/**
	 * Prepare this type for use.
	 *
	 * @param defs
	 *   definitions
	 */
	void prepare(ModelDefs defs);

	/**
	 * Get if this object has already been prepared.
	 *
	 * @return
	 */
	boolean isReady();

	/**
	 * Prepare the given object for use.
	 *
	 * @param o
	 * @param defs
	 */
	static void maybePrepare(Object o, ModelDefs defs)
	{
		if(o instanceof HasPreparation)
		{
			var casted = (HasPreparation) o;
			if(! casted.isReady())
			{
				casted.prepare(defs);
			}
		}
	}
}
