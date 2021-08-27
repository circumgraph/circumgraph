package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.InputFieldDefImpl;

/**
 * Field within a {@link InputObjectDef}.
 */
public interface InputFieldDef
	extends Derivable<InputFieldDef.Builder>, HasDirectives, HasLocation, HasMetadata
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
	 * Get the default value for this field.
	 *
	 * @return
	 */
	Optional<Object> getDefaultValue();

	/**
	 * Get the type that original declared this type.
	 *
	 * @return
	 */
	InputObjectDef getDeclaringType();

	/**
	 * Start building a new input field based on this instance.
	 *
	 * @return
	 */
	@Override
	Builder derive();

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
		extends Buildable<InputFieldDef>,
			HasDirectives.Builder<Builder>,
			HasLocation.Builder<Builder>,
			HasMetadata.Builder<Builder>
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
		@Override
		InputFieldDef build();
	}
}
