package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.EnumValueDefImpl;

/**
 * Value inside a {@link EnumDef}.
 */
public interface EnumValueDef
	extends Derivable<EnumValueDef.Builder>, HasDirectives, HasSourceLocation, HasMetadata
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
	@Override
	Builder derive();

	static Builder create(String name)
	{
		return EnumValueDefImpl.create(name);
	}

	interface Builder
		extends Buildable<EnumValueDef>,
			HasDirectives.Builder<Builder>,
			HasSourceLocation.Builder<Builder>,
			HasMetadata.Builder<Builder>
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
		@Override
		EnumValueDef build();
	}
}
