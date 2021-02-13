package com.circumgraph.model;

import java.util.Optional;

/**
 * Type definition in the Circumgraph model. This is the root of all types
 * in the model.
 */
public interface TypeDef
{
	/**
	 * Get the name of the type.
	 *
	 * @return
	 *   the unique name of this type
	 */
	String getName();

	/**
	 * Get the description of this data.
	 *
	 * @return
	 *   optional containing the description of the type
	 */
	Optional<String> getDescription();
}
