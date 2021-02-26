package com.circumgraph.model;

import java.util.Optional;

import com.circumgraph.model.internal.ModelBuilderImpl;
import com.circumgraph.model.validation.DirectiveValidator;

import org.eclipse.collections.api.list.ListIterable;

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
	 * Get all the types in the model.
	 *
	 * @return
	 */
	ListIterable<? extends TypeDef> getTypes();

	/**
	 * Get types that directly implement a type with the given identifier.
	 *
	 * @param id
	 * @return
	 */
	ListIterable<? extends StructuredDef> getImplements(String id);

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
		 * Add a validator for a certain type of directive.
		 *
		 * @param validator
		 * @return
		 */
		Builder addDirectiveValidator(DirectiveValidator<?> validator);

		/**
		 * Add a type to this builder.
		 *
		 * @param type
		 * @return
		 */
		Builder addType(TypeDef type);

		/**
		 * Add types using a schema.
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
