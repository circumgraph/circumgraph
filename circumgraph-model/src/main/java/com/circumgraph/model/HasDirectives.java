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
	ListIterable<DirectiveUse> getDirectives();

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Add a directive.
		 *
		 * @param directive
		 * @return
		 */
		B addDirective(DirectiveUse directive);

		/**
		 * Add several directives.
		 *
		 * @param directives
		 * @return
		 */
		B addDirectives(Iterable<? extends DirectiveUse> directives);
	}
}
