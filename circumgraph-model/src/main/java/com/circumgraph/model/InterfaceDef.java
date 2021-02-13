package com.circumgraph.model;

import com.circumgraph.model.internal.InterfaceDefImpl;

/**
 * Interface within the model. Interfaces are a type of structured data that
 * {@link ObjectDef objects} can implement.
 */
public interface InterfaceDef
	extends StructuredDef
{
	/**
	 * Create a builder that will create a {@link InterfaceDef}.
	 *
	 * @param name
	 * @return
	 */
	public static Builder create(String name)
	{
		return InterfaceDefImpl.create(name);
	}

	/**
	 * Builder for instances of {@link InterfaceDef}.
	 */
	interface Builder
		extends StructuredDef.Builder<Builder>
	{
		/**
		 * Build the instance.
		 *
		 * @return
		 */
		InterfaceDef build();
	}
}
