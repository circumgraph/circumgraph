package com.circumgraph.model.validation;

/**
 * Builder for validation messages.
 */
public interface ValidationBuilder
{
	/**
	 * Set the location where this validation message is created.
	 *
	 * @param location
	 * @return
	 */
	ValidationBuilder withLocation(SourceLocation location);

	/**
	 * Set a the human-readable message.
	 *
	 * @param message
	 * @param args
	 * @return
	 */
	ValidationBuilder withMessage(String message, Object... args);

	/**
	 * Set the code of the message.
	 *
	 * @param code
	 * @return
	 */
	ValidationBuilder withCode(String code);

	/**
	 * Add an argument to the message.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	ValidationBuilder withArgument(String key, Object value);

	/**
	 * Indicate that this message is done.
	 *
	 */
	void done();
}
