package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.EnumDefImpl;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Definition of a enum.
 */
public interface EnumDef
	extends SimpleValueDef, Derivable<EnumDef.Builder>, HasDirectives, HasLocation, HasMetadata
{
	/**
	 * Get the values of this enum.
	 *
	 * @return
	 */
	ListIterable<EnumValueDef> getValues();

	/**
	 * Get a specific value from this enum if it exists.
	 *
	 * @param name
	 * @return
	 */
	Optional<EnumValueDef> getValue(String name);

	/**
	 * Start building a new enum type based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

	static Builder create(String name)
	{
		return EnumDefImpl.create(name);
	}

	interface Builder
		extends Buildable<EnumDef>,
			HasDirectives.Builder<Builder>,
			HasLocation.Builder<Builder>,
			HasMetadata.Builder<Builder>
	{
		/**
		 * Set the description of this enum.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Add value to the enum.
		 *
		 * @param value
		 * @return
		 */
		Builder addValue(EnumValueDef value);

		/**
		 * Add multiple values to this enum.
		 *
		 * @param values
		 * @return
		 */
		Builder addValues(Iterable<? extends EnumValueDef> values);

		/**
		 * Build the value.
		 *
		 * @return
		 */
		@Override
		EnumDef build();
	}
}
