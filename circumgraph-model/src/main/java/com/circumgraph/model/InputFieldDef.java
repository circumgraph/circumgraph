package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.InputFieldDefImpl;

/**
 * Field within a {@link InputObjectDef}.
 */
public interface InputFieldDef
	extends HasDirectives, HasSourceLocation, HasMetadata
{
	/**
	 * Get the name of the input field.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the description of this argument.
	 *
	 * @return
	 */
	Optional<String> getDescription();

	/**
	 * Get the type of data.
	 *
	 * @return
	 */
	InputTypeDef getType();

	/**
	 * Get the type name.
	 *
	 * @return
	 */
	String getTypeName();

	/**
	 * Start building a new instance of {@link InputFieldDef}.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return InputFieldDefImpl.create(name);
	}

	interface Builder
		extends HasDirectives.Builder<Builder>, HasSourceLocation.Builder<Builder>
	{
		/**
		 * Set the description of this field.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Set the type using a known name.
		 *
		 * @param type
		 * @return
		 */
		Builder withType(String type);

		/**
		 * Set the type.
		 *
		 * @param type
		 * @return
		 */
		Builder withType(InputTypeDef type);

		/**
		 * Set the default value.
		 *
		 * @param defaultValue
		 * @return
		 */
		Builder withDefaultValue(Object defaultValue);

		/**
		 * Build the field.
		 *
		 * @return
		 */
		InputFieldDef build();
	}
}
