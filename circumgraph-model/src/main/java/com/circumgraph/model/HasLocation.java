package com.circumgraph.model;

/**
 * Marker interface for types that support a {@link Location}.
 */
public interface HasLocation
{
	/**
	 * Get the source of this object. This is used to provide debug
	 * information during validation of the model.
	 *
	 * @return
	 *   source of the definition
	 */
	Location getDefinedAt();

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Set a specific source location of the object. Used for example when
		 * parsing external schemas.
		 *
		 * @param location
		 *   the location to use
		 * @return
		 *   new instance of build
		 */
		B withDefinedAt(Location location);
	}
}
