package com.circumgraph.model.validation;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.HasDirectives;

/**
 * Validator for a directive.
 */
public interface DirectiveValidator<T extends HasDirectives>
{
	/**
	 * Get the name of the directive this validates.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the type of context this validator requires.
	 *
	 * @return
	 */
	Class<T> getContextType();

	/**
	 * Validate the directive.
	 *
	 * @param location
	 * @param directive
	 * @param validationCollector
	 */
	void validate(
		T location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	);
}
