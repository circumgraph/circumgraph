package com.circumgraph.model.validation;

import com.circumgraph.model.HasSourceLocation;
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
	 * Start building a new validation message.
	 *
	 * @param type
	 * @return
	 */
	static Builder create(ValidationMessageType type)
	{
		return ValidationMessageImpl.create(type);
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
		 * Set the location where this validation message is created.
		 *
		 * @param object
		 * @return
		 */
		Builder withLocation(HasSourceLocation object);

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
