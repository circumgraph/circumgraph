package com.circumgraph.model;

import com.circumgraph.model.internal.DirectiveUseImpl;

/**
 * Directive as applied to a {@link TypeDef type}, {@link FieldDef field}
 * or {@link ArgumentDef argument}.
 */
public interface DirectiveUse
	extends HasSourceLocation, HasArgumentUse
{
	/**
	 * Get the name of the directive.
	 *
	 * @return
	 */
	String getName();

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

	interface Builder
		extends HasSourceLocation.Builder<Builder>, HasArgumentUse.Builder<Builder>
	{
		/**
		 * Build the instance.
		 *
		 * @return
		 */
		DirectiveUse build();
	}
}
