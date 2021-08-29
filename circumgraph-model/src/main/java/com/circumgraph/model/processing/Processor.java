package com.circumgraph.model.processing;

import com.circumgraph.model.Location;

/**
 * Processor interface, shared between more specific processors.
 */
public interface Processor
{
	/**
	 * Get the location of the processor. This is used for types being added,
	 * replaced and edited.
	 *
	 * @return
	 */
	Location getLocation();
}
