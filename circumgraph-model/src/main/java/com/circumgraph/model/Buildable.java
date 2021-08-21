package com.circumgraph.model;

/**
 * Marker interface for things that can be built.
 */
public interface Buildable<D>
{
	/**
	 * Build the instance.
	 *
	 * @return
	 */
	D build();
}
