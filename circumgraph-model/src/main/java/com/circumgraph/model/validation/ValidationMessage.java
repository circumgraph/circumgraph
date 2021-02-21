package com.circumgraph.model.validation;

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
}
