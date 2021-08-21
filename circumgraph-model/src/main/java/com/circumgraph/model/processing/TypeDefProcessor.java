package com.circumgraph.model.processing;

import com.circumgraph.model.TypeDef;

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
	 * @param encounter
	 *   encounter used to report validation issues and modify the schema
	 * @param type
	 *   type being processed
	 */
	void process(
		ProcessingEncounter encounter,
		T type
	);
}
