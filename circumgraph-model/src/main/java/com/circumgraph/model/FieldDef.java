package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.FieldDefImpl;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Field within an entity.
 */
public interface FieldDef
	extends HasDirectives, HasSourceLocation, HasMetadata
{
	/**
	 * Get the name of this field.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the description of this field.
	 *
	 * @return
	 */
	Optional<String> getDescription();

	/**
	 * Get the type of data.
	 *
	 * @return
	 */
	OutputTypeDef getType();

	/**
	 * Get the type name used by the field.
	 *
	 * @return
	 */
	String getTypeName();

	/**
	 * Get the arguments of this field.
	 *
	 * @return
	 */
	ListIterable<ArgumentDef> getArguments();

	/**
	 * Start building a new field.
	 *
	 * @param name
	 * @return
	 */
	static Builder create(String name)
	{
		return FieldDefImpl.create(name);
	}

	interface Builder
		extends HasDirectives.Builder<Builder>, HasSourceLocation.Builder<Builder>
	{
		/**
		 * Set the description of this field.
		 *
		 * @param description
		 * @return
		 */
		Builder withDescription(String description);

		/**
		 * Set the type using a known name.
		 *
		 * @param type
		 * @return
		 */
		Builder withType(String type);

		/**
		 * Set the type.
		 *
		 * @param type
		 * @return
		 */
		Builder withType(OutputTypeDef type);

		/**
		 * Add an argument to the field.
		 *
		 * @param arg
		 * @return
		 */
		Builder addArgument(ArgumentDef arg);

		/**
		 * Add several arguments to the field.
		 *
		 * @param args
		 * @return
		 */
		Builder addArguments(Iterable<? extends ArgumentDef> args);

		/**
		 * Build the field.
		 *
		 * @return
		 */
		FieldDef build();
	}
}
