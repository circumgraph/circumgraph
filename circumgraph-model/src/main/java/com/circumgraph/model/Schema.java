package com.circumgraph.model;

import java.util.function.Consumer;

import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;

import org.eclipse.collections.api.factory.Lists;

/**
 * Abstraction of a schema that contains several types.
 */
public interface Schema
{
	/**
	 * Get the types in this schema.
	 *
	 * @return
	 */
	default Iterable<? extends TypeDef> getTypes()
	{
		return Lists.immutable.empty();
	}

	/**
	 * Get validators for directives.
	 *
	 * @return
	 */
	default Iterable<? extends DirectiveValidator<?>> getDirectiveValidators()
	{
		return Lists.immutable.empty();
	}

	/**
	 * Validate the given type.
	 *
	 * @param type
	 * @param validationCollector
	 */
	default void validate(TypeDef type, Consumer<ValidationMessage> validationCollector)
	{
	}
}
