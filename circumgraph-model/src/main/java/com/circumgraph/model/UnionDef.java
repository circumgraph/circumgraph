package com.circumgraph.model;

import com.circumgraph.model.internal.UnionDefImpl;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Definition of a union of several types.
 */
public interface UnionDef
	extends OutputTypeDef, Derivable<UnionDef.Builder>, HasDirectives, HasSourceLocation, HasMetadata
{
	/**
	 * Get the types that can be within the union.
	 *
	 * @return
	 */
	ListIterable<? extends OutputTypeDef> getTypes();

	/**
	 * Get the names of types this union can hold.
	 *
	 * @return
	 */
	ListIterable<String> getTypeNames();

	/**
	 * Start building a new union type based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

	/**
	 * Start building a union type.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return UnionDefImpl.create(name);
	}

	interface Builder
		extends Buildable<UnionDef>,
			HasDirectives.Builder<Builder>,
			HasSourceLocation.Builder<Builder>,
			HasMetadata.Builder<Builder>
	{
		/**
		 * Set the description of this union.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Add type to the union.
		 *
		 * @param value
		 * @return
		 */
		Builder addType(OutputTypeDef value);

		/**
		 * Add multiple types to this union.
		 *
		 * @param types
		 * @return
		 */
		Builder addTypes(Iterable<? extends OutputTypeDef> types);

		/**
		 * Build the value.
		 *
		 * @return
		 */
		@Override
		UnionDef build();
	}
}
