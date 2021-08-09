package com.circumgraph.model;

import com.circumgraph.model.internal.SchemaImpl;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.TypeDefProcessor;

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
	 * Get processors for directives.
	 *
	 * @return
	 */
	default Iterable<? extends DirectiveUseProcessor<?>> getDirectiveUseProcessors()
	{
		return Lists.immutable.empty();
	}

	/**
	 * Get processors of types.
	 *
	 * @return
	 */
	default Iterable<? extends TypeDefProcessor<?>> getTypeDefProcessors()
	{
		return Lists.immutable.empty();
	}

	/**
	 * Start building a schema.
	 *
	 * @return
	 */
	static Builder create()
	{
		return SchemaImpl.create();
	}

	/**
	 * Builder for instances of {@link Schema}.
	 */
	interface Builder
	{
		/**
		 * Add a processor for a certain type of directive.
		 *
		 * @param processor
		 * @return
		 */
		Builder addDirectiveUseProcessor(DirectiveUseProcessor<?> processor);

		/**
		 * Add several processors for directives.
		 *
		 * @param processors
		 * @return
		 */
		Builder addDirectiveUseProcessors(Iterable<? extends DirectiveUseProcessor<?>> processors);

		/**
		 * Add a processor of types.
		 *
		 * @param processor
		 * @return
		 */
		Builder addTypeDefProcessor(TypeDefProcessor<?> processor);

		/**
		 * Add several processors for types.
		 *
		 * @param processors
		 * @return
		 */
		Builder addTypeDefProcessors(Iterable<? extends TypeDefProcessor<?>> processors);

		/**
		 * Add a type to the schema.
		 *
		 * @param type
		 * @return
		 */
		Builder addType(TypeDef type);

		/**
		 * Add several types to the schema.
		 *
		 * @param types
		 * @return
		 */
		Builder addTypes(Iterable<? extends TypeDef> types);

		/**
		 * Return the schema.
		 *
		 * @return
		 */
		Schema build();
	}
}
