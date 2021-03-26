package com.circumgraph.model.validation;

import com.circumgraph.model.internal.validation.ValidationMessageImpl;

import org.eclipse.collections.api.map.MapIterable;

/**
 * Message containing validation information.
 */
public interface ValidationMessage
{
	/**
	 * Get the location of this message.
	 *
	 * @return
	 */
	SourceLocation getLocation();

	/**
	 * Get the level of this message.
	 *
	 * @return
	 */
	ValidationMessageLevel getLevel();

	/**
	 * Get human readable description of the validation error.
	 *
	 * @return
	 */
	String getMessage();

	/**
	 * Get the code.
	 *
	 * @return
	 */
	String getCode();

	/**
	 * Get the arguments.
	 *
	 * @return
	 */
	MapIterable<String, Object> getArguments();

	/**
	 * Start building a new validation message with the given level.
	 *
	 * @param level
	 * @return
	 */
	static Builder create(ValidationMessageLevel level)
	{
		return ValidationMessageImpl.create(level);
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
		 * Set the location where this validation message is created.
		 *
		 * @param location
		 * @return
		 */
		Builder withLocation(SourceLocation location);

		/**
		 * Set a the human-readable message.
		 *
		 * @param message
		 * @param args
		 * @return
		 */
		Builder withMessage(String message, Object... args);

		/**
		 * Set the code of the message.
		 *
		 * @param code
		 * @return
		 */
		Builder withCode(String code);

		/**
		 * Add an argument to the message.
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		Builder withArgument(String key, Object value);

		/**
		 * Indicate that this message is done.
		 *
		 */
		ValidationMessage build();
	}
}
