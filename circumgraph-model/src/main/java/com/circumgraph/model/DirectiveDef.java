package com.circumgraph.model;

/**
 * Directive as applied to a {@link TypeDef type}, {@link FieldDef field}
 * or {@link ArgumentDef argument}.
 */
public interface DirectiveDef
{
	/**
	 * Get the name of the directive.
	 */
	String getName();
}
