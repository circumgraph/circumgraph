package com.circumgraph.model;

import java.util.Optional;

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

	/**
	 * Get a directive with the given name.
	 *
	 * @param name
	 * @return
	 */
	default Optional<DirectiveUse> getDirective(String name)
	{
		return getDirectives().detectOptional(d -> d.getName().equals(name));
	}

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
