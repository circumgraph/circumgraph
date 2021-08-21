package com.circumgraph.model.processing;

import java.util.function.Function;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.Buildable;
import com.circumgraph.model.Derivable;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputFieldDef;
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
	 * Edit something in the model. Can be used to edit {@link TypeDef types},
	 * {@link FieldDef fields}, {@link ArgumentDef arguments} and
	 * {@link InputFieldDef input fields}.
	 *
	 * @param <B>
	 * @param <D>
	 * @param editor
	 */
	<B extends Buildable<?>, D extends Derivable<B>> void edit(
		D instance,
		Function<B, B> editor
	);
}
