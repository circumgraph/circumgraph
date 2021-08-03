package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.ModelBuilderImpl;
import com.circumgraph.model.processing.DirectiveUseProcessor;

import org.eclipse.collections.api.set.SetIterable;

/**
 * Model that Circumgraph uses to generate a storage, index and APIs.
 * Internally this follows the GraphQL specification closely.
 *
 * <p>
 * The root of all types is {@link TypeDef}. {@link StructuredDef} is a common
 * base for structured data, with {@link InterfaceDef} used for interfaces and
 * {@link ObjectDef} for full objects.
 */
public interface Model
{
	/**
	 * Get a type by its name.
	 *
	 * @param id
	 * @return
	 */
	Optional<TypeDef> get(String id);

	/**
	 * Get a type by its name and type.
	 *
	 * @param <T>
	 * @param id
	 * @param type
	 * @return
	 */
	<T extends TypeDef> Optional<T> get(String id, Class<T> type);

	/**
	 * Get all the types in the model.
	 *
	 * @return
	 */
	SetIterable<? extends TypeDef> getTypes();

	/**
	 * Get types that directly implement a type with the given identifier.
	 *
	 * @param id
	 * @return
	 */
	SetIterable<? extends StructuredDef> getImplements(String id);

	/**
	 * Find all types that implement a specific type.
	 *
	 * @param id
	 * @return
	 */
	SetIterable<? extends StructuredDef> findImplements(String id);

	/**
	 * Start building a new model.
	 *
	 * @return
	 */
	static Builder create()
	{
		return ModelBuilderImpl.create();
	}

	/**
	 * Builder for instances of {@link Model}.
	 */
	interface Builder
	{
		/**
		 * Add a schema.
		 *
		 * @param builder
		 * @return
		 */
		Builder addSchema(Schema schema);

		/**
		 * Resolve and build the model.
		 *
		 * @return
		 */
		Model build();
	}
}
