package com.circumgraph.model;

import java.util.Optional;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Interface used to mark things in the model that support arguments.
 *
 */
public interface HasArgumentUse
{
	/**
	 * Get the arguments of this directive.
	 *
	 * @return
	 */
	ListIterable<ArgumentUse> getArguments();

	/**
	 * Get an argument via name.
	 *
	 * @param name
	 * @return
	 */
	default Optional<ArgumentUse> getArgument(String name)
	{
		return getArguments().detectOptional(p -> p.getName().equals(name));
	}

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Add an argument.
		 *
		 * @param argument
		 * @return
		 */
		B addArgument(ArgumentUse argument);

		/**
		 * Add an argument.
		 *
		 * @param name
		 * @param value
		 * @return
		 */
		default B addArgument(String name, Object value)
		{
			return addArgument(ArgumentUse.create(name, value));
		}

		/**
		 * Add several arguments.
		 *
		 * @param arguments
		 * @return
		 */
		B addArguments(Iterable<? extends ArgumentUse> arguments);
	}
}
