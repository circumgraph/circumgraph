package com.circumgraph.model;

import com.circumgraph.model.internal.EnumDefImpl;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Definition of a enum.
 */
public interface EnumDef
	extends SimpleValueDef, HasDirectives, HasSourceLocation
{
	/**
	 * Get the values of this enum.
	 *
	 * @return
	 */
	ListIterable<EnumValueDef> getValues();

	static Builder create(String name)
	{
		return EnumDefImpl.create(name);
	}

	interface Builder
		extends HasDirectives.Builder<Builder>, HasSourceLocation.Builder<Builder>
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
		EnumDef build();
	}
}
