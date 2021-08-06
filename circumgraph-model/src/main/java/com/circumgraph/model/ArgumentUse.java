package com.circumgraph.model;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.circumgraph.model.internal.ArgumentUseImpl;

/**
 * Argument being used, such as in a {@link DirectiveUse}.
 */
public interface ArgumentUse
{
	/**
	 * Get the name of the argument.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the value of the argument.
	 *
	 * @return
	 */
	Object getValue();

	/**
	 * Get the value as a string.
	 *
	 * @return
	 */
	Optional<String> getValueAsString();

	/**
	 * Get the value as an integer.
	 *
	 * @return
	 */
	OptionalInt getValueAsInt();

	/**
	 * Get the value as a long.
	 *
	 * @return
	 */
	OptionalLong getValueAsLong();

	/**
	 * Get the value as a double.
	 *
	 * @return
	 */
	OptionalDouble getValueAsDouble();

	/**
	 * Start creating a new instance of {@link ArgumentUse}.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return ArgumentUseImpl.create(name);
	}

	/**
	 * Create an instance of {@link ArgumentUse}.
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static ArgumentUse create(String name, Object value)
	{
		return new ArgumentUseImpl(name, value);
	}

	interface Builder
	{
		/**
		 * Set the value of the argument.
		 *
		 * @param value
		 * @return
		 */
		Builder withValue(Object value);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		ArgumentUse build();
	}
}
