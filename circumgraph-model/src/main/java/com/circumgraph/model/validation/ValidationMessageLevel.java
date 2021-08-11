package com.circumgraph.model.validation;

/**
 * Different types of messages.
 *
 * Note: Enum values are declared in increasing severity as this is used by
 * {@link ValidationMessage#predicate(ValidationMessageLevel)} to match
 * messages.
 */
public enum ValidationMessageLevel
{
	/**
	 * Message indicates information.
	 */
	INFO,

	/**
	 * Message indicates a warning that may be fixed.
	 */
	WARNING,

	/**
	 * Message indicates an error that must be fixed.
	 */
	ERROR
}
