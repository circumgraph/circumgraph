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

	/**
	 * Get if the other type is assignable to something that is declared as
	 * this type.
	 *
	 * @param other
	 *   other type to check
	 * @return
	 *   {@code true} if something declared as this type can be assigned the
	 *   other type
	 */
	boolean isAssignableFrom(TypeDef other);
}
