package com.circumgraph.model;

import com.circumgraph.model.validation.SourceLocation;

/**
 * Marker interface for types that support a {@link SourceLocation}.
 */
public interface HasSourceLocation
{
	/**
	 * Get the source of this object. This is used to provide debug
	 * information during validation of the model.
	 *
	 * @return
	 *   source of the definition
	 */
	SourceLocation getSourceLocation();

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Set a specific source location of the object. Used for example when
		 * parsing external schemas.
		 *
		 * @param sourceLocation
		 *   the location to use
		 * @return
		 *   new instance of build
		 */
		B withSourceLocation(SourceLocation sourceLocation);
	}
}
