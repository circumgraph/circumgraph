package com.circumgraph.model;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.set.SetIterable;

/**
 * Data that is structured. Has two implementations {@link ObjectDef} and
 * {@link InterfaceDef}.
 */
public interface StructuredDef
	extends OutputTypeDef, HasDirectives, HasSourceLocation, HasMetadata
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
	 * Get if this type directly implements the given type.
	 *
	 * @return
	 */
	boolean hasImplements(String name);

	/**
	 * Get all of the interfaces implemented by this def.
	 *
	 * @return
	 */
	SetIterable<InterfaceDef> getAllImplements();

	/**
	 * Find if this type somehow implements the given type.
	 *
	 * @param name
	 * @return
	 */
	boolean findImplements(String name);

	/**
	 * Find a specific {@link InterfaceDef} this type implements directly or
	 * indirectly based on the given predicate.
	 *
	 * @param predicate
	 * @return
	 */
	Optional<InterfaceDef> findImplements(Predicate<InterfaceDef> predicate);

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
	 * @param name
	 *   name of field
	 * @return
	 */
	Optional<FieldDef> getField(String name);

	/**
	 * Get a field using a path, this will descend into fields delimited by
	 * a period ({@code .}).
	 *
	 * @param path
	 *   path to get
	 * @return
	 *   field if it exists
	 */
	Optional<FieldDef> pickField(String path);

	/**
	 * Start building a new structured type based on this instance.
	 *
	 * @return
	 */
	Builder<?> derive();

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
