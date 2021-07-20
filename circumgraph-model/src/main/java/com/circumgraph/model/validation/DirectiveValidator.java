package com.circumgraph.model.validation;

import java.util.function.Consumer;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.HasDirectives;

import org.eclipse.collections.api.factory.Sets;

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

	/**
	 * Check that no arguments except the given ones exist on the directive.
	 *
	 * @param use
	 * @param names
	 * @return
	 */
	static boolean checkOnlyArguments(DirectiveUse use, String... names)
	{
		var set = Sets.mutable.of(names);
		for(ArgumentUse arg : use.getArguments())
		{
			if(! set.contains(arg.getName()))
			{
				return false;
			}
		}

		return true;
	}
}
