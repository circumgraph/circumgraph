package com.circumgraph.model.internal;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
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
		if(o instanceof HasPreparation p)
		{
			p.prepare(defs);
		}
	}

	/**
	 * Prepare the given object for use, but only if its an unnamed type
	 * such as {@link NonNullDef} or {@link ListDef}.
	 *
	 * @param o
	 * @param defs
	 */
	static void prepareUnnamed(Object o, ModelDefs defs)
	{
		if(o instanceof HasPreparation p && (o instanceof NonNullDef || o instanceof ListDef))
		{
			p.prepare(defs);
		}
	}
}
