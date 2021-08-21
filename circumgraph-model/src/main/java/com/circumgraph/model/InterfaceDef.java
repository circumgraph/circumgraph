package com.circumgraph.model;

import com.circumgraph.model.internal.InterfaceDefImpl;

import org.eclipse.collections.api.set.SetIterable;

/**
 * Interface within the model. Interfaces are a type of structured data that
 * {@link ObjectDef objects} can implement.
 */
public interface InterfaceDef
	extends StructuredDef, Derivable<InterfaceDef.Builder>
{
	/**
	 * Get types that directly implements this interface.
	 *
	 * @return
	 */
	SetIterable<? extends StructuredDef> getImplementors();

	/**
	 * Get all the types that implements this interface.
	 *
	 * @return
	 */
	SetIterable<? extends StructuredDef> getAllImplementors();

	/**
	 * Start building a new interface type based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

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
		extends StructuredDef.Builder<Builder>, Buildable<InterfaceDef>
	{
		/**
		 * Build the instance.
		 *
		 * @return
		 */
		@Override
		InterfaceDef build();
	}
}
