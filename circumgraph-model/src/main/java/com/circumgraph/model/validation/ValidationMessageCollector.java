package com.circumgraph.model.validation;

import java.util.function.Consumer;

import com.circumgraph.model.internal.validation.ValidationMessageCollectorImpl;

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

	/**
	 * Create a collector that will receive instances of {@link ValidationMessage}.
	 *
	 * @param consumer
	 * @return
	 */
	static ValidationMessageCollector create(Consumer<ValidationMessage> consumer)
	{
		return new ValidationMessageCollectorImpl(consumer);
	}
}
