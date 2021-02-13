package com.circumgraph.model;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Marker interface for types, fields, arguments and anything that can have
 * directives applied to them.
 */
public interface HasDirectives
{
	/**
	 * Get the directives present.
	 *
	 * @return
	 */
	ListIterable<DirectiveDef> getDirectives();

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Add a directive.
		 *
		 * @param directive
		 * @return
		 */
		B addDirective(DirectiveDef directive);
	}
}
