package com.circumgraph.model;

import java.util.Optional;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Data that is structured. Has two implementations {@link ObjectDef} and
 * {@link InterfaceDef}.
 */
public interface StructuredDef
	extends OutputTypeDef, HasDirectives, HasSourceLocation
{
	/**
	 * Get the names of types this structured data implements.
	 *
	 * @return
	 */
	ListIterable<String> getImplementsNames();

	/**
	 * Get what types this data implements.
	 *
	 * @return
	 */
	ListIterable<InterfaceDef> getImplements();

	/**
	 * Get the all the fields in this object.
	 *
	 * @return
	 */
	RichIterable<FieldDef> getFields();

	/**
	 * Get the fields directly present on this object.
	 *
	 * @return
	 */
	RichIterable<FieldDef> getDirectFields();

	/**
	 * Get field using its name.
	 *
	 * @return
	 */
	Optional<FieldDef> getField(String name);

	/**
	 * Builder for instances of {@link StructuredDef}.
	 */
	interface Builder<B extends Builder<B>>
		extends HasDirectives.Builder<B>, HasSourceLocation.Builder<B>
	{
		/**
		 * Add that this structured data implements the given type.
		 *
		 * @param typeId
		 * @return
		 */
		B addImplements(String typeId);

		/**
		 * Add that this structured data implements the given type.
		 *
		 * @param type
		 * @return
		 */
		B addImplements(TypeRef type);

		/**
		 * Add that this structured data implements all of the given types.
		 *
		 * @param types
		 * @return
		 */
		B addImplementsAll(Iterable<String> types);

		/**
		 * Set the description of the type.
		 *
		 * @param description
		 * @return
		 */
		B withDescription(String description);

		/**
		 * Add a field to the structured type.
		 *
		 * @param field
		 * @return
		 */
		B addField(FieldDef field);

		/**
		 * Add several fields to the structured type.
		 *
		 * @param fields
		 * @return
		 */
		B addFields(Iterable<? extends FieldDef> fields);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		StructuredDef build();
	}
}
