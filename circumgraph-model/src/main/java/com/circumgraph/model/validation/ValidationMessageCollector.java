package com.circumgraph.model.validation;

/**
 * Collector used for validation messages.
 */
@FunctionalInterface
public interface ValidationMessageCollector
{
	/**
	 * Start adding a new validation messages.
	 *
	 * @param level
	 */
	ValidationBuilder add(ValidationMessageLevel level);

	default ValidationBuilder info()
	{
		return add(ValidationMessageLevel.INFO);
	}

	default ValidationBuilder warn()
	{
		return add(ValidationMessageLevel.WARNING);
	}

	default ValidationBuilder error()
	{
		return add(ValidationMessageLevel.ERROR);
	}
}
