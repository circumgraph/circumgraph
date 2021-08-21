package com.circumgraph.model;

public interface Derivable<B extends Buildable<?>>
{
	/**
	 * Start building a new type based on this one.
	 *
	 * @return
	 */
	B derive();
}
