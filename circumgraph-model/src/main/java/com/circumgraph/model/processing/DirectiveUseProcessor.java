package com.circumgraph.model.processing;

import java.util.function.Consumer;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.validation.ValidationMessage;

import org.eclipse.collections.api.factory.Sets;

/**
 * Processor that runs when a directive of a certain name is used. This type
 * of processor should interpret the directive and update {@link HasMetadata metadata}
 * for the location as needed.
 */
public interface DirectiveUseProcessor<T extends HasDirectives>
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
	 * Process the directive.
	 *
	 * @param location
	 *   location of the directive
	 * @param directive
	 *   the use of the directive
	 * @param validationCollector
	 *   collector for validation issues that occur during the processing
	 */
	void process(
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
