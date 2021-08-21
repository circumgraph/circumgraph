package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.InputObjectDefImpl;

import org.eclipse.collections.api.RichIterable;

/**
 * Definition of an input object.
 */
public interface InputObjectDef
	extends InputTypeDef, Derivable<InputObjectDef.Builder>, HasSourceLocation, HasDirectives, HasMetadata
{
	/**
	 * Get the all the fields in this object.
	 *
	 * @return
	 */
	RichIterable<InputFieldDef> getFields();

		/**
	 * Get field using its name.
	 *
	 * @param name
	 *   name of field
	 * @return
	 */
	Optional<InputFieldDef> getField(String name);

	/**
	 * Start building a new input object type based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

	/**
	 * Start creating a new instance.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return InputObjectDefImpl.create(name);
	}

	/**
	 * Builder for instances of {@link InputObjectDef}.
	 */
	interface Builder
		extends Buildable<InputObjectDef>,
			HasDirectives.Builder<Builder>,
			HasSourceLocation.Builder<Builder>,
			HasMetadata.Builder<Builder>
	{
		/**
		 * Set the description of the type.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Add a field to the input object.
		 *
		 * @param field
		 * @return
		 */
		Builder addField(InputFieldDef field);

		/**
		 * Add several fields input object.
		 *
		 * @param fields
		 * @return
		 */
		Builder addFields(Iterable<? extends InputFieldDef> fields);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		@Override
		InputObjectDef build();
	}
}
