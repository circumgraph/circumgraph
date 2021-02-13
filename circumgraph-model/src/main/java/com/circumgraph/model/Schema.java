package com.circumgraph.model;

/**
 * Abstraction of a schema that contains several types.
 */
public interface Schema
{
	/**
	 * Get the types in this schema.
	 *
	 * @return
	 */
	Iterable<? extends TypeDef> getTypes();
}
