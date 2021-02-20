package com.circumgraph.model;

import com.circumgraph.model.internal.DirectiveUseImpl;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Directive as applied to a {@link TypeDef type}, {@link FieldDef field}
 * or {@link ArgumentDef argument}.
 */
public interface DirectiveUse
	extends HasSourceLocation
{
	/**
	 * Get the name of the directive.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the arguments of this directive.
	 *
	 * @return
	 */
	ListIterable<Argument> getArguments();

	/**
	 * Start building a new instance.
	 *
	 * @param name
	 * @return
	 */
	public static Builder create(String name)
	{
		return DirectiveUseImpl.create(name);
	}

	interface Argument
	{
		/**
		 * Get the name of this argument.
		 *
		 * @return
		 */
		String getName();

		/**
		 * Get the value of this argument.
		 *
		 * @return
		 */
		Object getValue();
	}

	interface Builder
		extends HasSourceLocation.Builder<Builder>
	{
		/**
		 * Add an argument to the directive.
		 *
		 * @param name
		 * @param value
		 * @return
		 */
		Builder addArgument(String name, Object value);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		DirectiveUse build();
	}
}
