package com.circumgraph.model.processing;

import java.util.function.Consumer;

import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.ValidationMessage;

/**
 * Processor for instances of {@link TypeDef}.
 */
public interface TypeDefProcessor<T>
{
	/**
	 * Get the type this can process.
	 *
	 * @return
	 */
	Class<T> getType();

	/**
	 * Process the type.
	 *
	 * @param type
	 *   type being processed
	 * @param validationCollector
	 *   collector for validation issues that occur during the processing
	 */
	void process(
		T type,
		Consumer<ValidationMessage> validationCollector
	);
}
