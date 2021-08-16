package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.EnumValueDefImpl;

/**
 * Value inside a {@link EnumDef}.
 */
public interface EnumValueDef
	extends HasDirectives, HasSourceLocation
{
	/**
	 * Get the name of the value.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the description of the value.
	 *
	 * @return
	 */
	Optional<String> getDescription();

	/**
	 * Start building a new enum value based on this instance.
	 *
	 * @return
	 */
	Builder derive();

	static Builder create(String name)
	{
		return EnumValueDefImpl.create(name);
	}

	interface Builder
		extends HasDirectives.Builder<Builder>, HasSourceLocation.Builder<Builder>
	{
		/**
		 * Set the description of this enum value.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Build the value.
		 *
		 * @return
		 */
		EnumValueDef build();
	}
}
