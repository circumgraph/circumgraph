package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.ArgumentDefImpl;

/**
 * Argument of a {@link FieldDef}.
 */
public interface ArgumentDef
	extends HasDirectives, HasSourceLocation
{
	/**
	 * Get the name of this argument.
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
	 * Get the default value.
	 *
	 * @return
	 */
	Optional<Object> getDefaultValue();

	public static Builder create(String name)
	{
		return ArgumentDefImpl.create(name);
	}

	interface Builder
		extends HasDirectives.Builder<Builder>, HasSourceLocation.Builder<Builder>
	{
		/**
		 * Set the the description of the argument.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Set the type of the argument.
		 *
		 * @param type
		 * @return
		 */
		Builder withType(InputTypeDef type);

		/**
		 * Add a directive to the argument.
		 *
		 * @param directive
		 * @return
		 */
		Builder addDirective(DirectiveUse directive);

		/**
		 * Set the default of the argument.
		 *
		 * @param value
		 *   default value
		 * @return
		 */
		Builder withDefaultValue(Object value);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		ArgumentDef build();
	}
}
