package com.circumgraph.model.validation;

/**
 * Different types of messages.
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
