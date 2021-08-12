package com.circumgraph.model.validation;

import com.circumgraph.model.internal.validation.ValidationMessageTypeImpl;

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.set.SetIterable;

/**
 * Definition of a type of validation message. Types define the level of
 * the message, a known code, arguments and a human readable message.
 */
public interface ValidationMessageType
{
	/**
	 * Get the level of this message.
	 *
	 * @return
	 */
	ValidationMessageLevel getLevel();

	/**
	 * Get the code.
	 *
	 * @return
	 */
	String getCode();

	/**
	 * Get the arguments that can be set for this type.
	 *
	 * @return
	 */
	SetIterable<String> getArguments();

	/**
	 * Format for the given arguments.
	 *
	 * @param arguments
	 * @return
	 */
	String format(MapIterable<String, Object> arguments);

	/**
	 * Turn this type into a message.
	 *
	 * @return
	 */
	ValidationMessage.Builder toMessage();

	/**
	 * Start building a new validation message with the given level.
	 *
	 * @param level
	 * @return
	 */
	static Builder create(ValidationMessageLevel level)
	{
		return ValidationMessageTypeImpl.create(level);
	}

	/**
	 * Start building a new {@link ValidationMessageLevel#INFO info} message.
	 *
	 * @return
	 */
	static Builder info()
	{
		return create(ValidationMessageLevel.INFO);
	}

	/**
	 * Start building a new {@link ValidationMessageLevel#WARNING warning}
	 * message.
	 *
	 * @return
	 */
	static Builder warn()
	{
		return create(ValidationMessageLevel.WARNING);
	}

	/**
	 * Start building a new {@link ValidationMessageLevel#ERROR error} message.
	 *
	 * @return
	 */
	static Builder error()
	{
		return create(ValidationMessageLevel.ERROR);
	}

	interface Builder
	{
		/**
		 * Set a the human-readable message.
		 *
		 * @param message
		 * @return
		 */
		Builder withMessage(String message);

		/**
		 * Set the code of the type.
		 *
		 * @param code
		 * @return
		 */
		Builder withCode(String code);

		/**
		 * Add an argument that should be set.
		 *
		 * @param key
		 * @return
		 */
		Builder withArgument(String key);

		/**
		 * Build the type.
		 *
		 * @return
		 */
		ValidationMessageType build();
	}
}
