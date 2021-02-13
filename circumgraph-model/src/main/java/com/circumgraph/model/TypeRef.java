package com.circumgraph.model;

import com.circumgraph.model.internal.TypeRefImpl;

/**
 * Reference to another type.
 */
public interface TypeRef
	extends InputTypeDef, OutputTypeDef
{
	/**
	 * Get a reference to a given type.
	 *
	 * @param id
	 * @return
	 */
	static TypeRef create(String id)
	{
		return new TypeRefImpl(id);
	}
}
