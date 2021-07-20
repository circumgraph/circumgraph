package com.circumgraph.model.validation;

import java.util.function.Consumer;

import com.circumgraph.model.ArgumentUse;

/**
 * Validation of a single {@link ArgumentUse}.
 */
public interface ArgumentValidator
{
	/**
	 * Validate argument.
	 *
	 * @param location
	 * @param arg
	 * @param validationCollector
	 */
	void validate(
		SourceLocation location,
		ArgumentUse arg,
		Consumer<ValidationMessage> validationCollector
	);

	interface Builder<T>
	{
		/**
		 * Validate that the argument can be turned into the given type.
		 *
		 * @param type
		 * @return
		 */
		<NT> Builder<T> withType(Class<NT> type);
	}
}
