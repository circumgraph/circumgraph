package com.circumgraph.model.processing;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.ValidationMessage;

/**
 * Encounter used when processing types, fields, directives etc.
 */
public interface ProcessingEncounter
{
	/**
	 * Report a validation issue.
	 *
	 * @param message
	 */
	void report(ValidationMessage message);

	/**
	 * Add an entirely new type to the schema. If the type already exists this
	 * will attempt to merge the types.
	 *
	 * @param type
	 *   the type to add
	 */
	void addType(TypeDef type);

	/**
	 * Replace a type entirely.
	 *
	 * @param type
	 *   new version of type
	 */
	void replaceType(TypeDef type);

	/**
	 * Change the output type of a field.
	 *
	 * @param field
	 *   field to modify
	 * @param def
	 *   the new output type of the field
	 */
	void changeOutput(FieldDef field, OutputTypeDef def);

	/**
	 * Add an argument to a field.
	 *
	 * @param field
	 *   field to modify
	 * @param arg
	 *   argument to add
	 */
	void addArgument(FieldDef field, ArgumentDef arg);
}
