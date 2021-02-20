package com.circumgraph.storage.mutation;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.internal.mutation.StructuredMutationImpl;

import org.eclipse.collections.api.map.MapIterable;

/**
 * Mutation for changing a structured value.
 */
public interface StructuredMutation
	extends Mutation
{
	/**
	 * Get the type of structured data being mutated.
	 */
	StructuredDef getType();

	/**
	 * Get the field that have been mutated.
	 *
	 * @return
	 */
	MapIterable<String, Mutation> getFields();

	/**
	 * Create a mutation on top of the given definition.
	 *
	 * @param def
	 * @return
	 */
	static Builder create(StructuredDef def)
	{
		return StructuredMutationImpl.create(def);
	}

	interface Builder
	{
		/**
		 * Add a mutation for the given field.
		 *
		 * @param field
		 *   the name of the field
		 * @param mutation
		 *   the mutation to apply
		 * @return
		 */
		Builder updateField(String field, Mutation mutation);

		/**
		 * Build the mutation.
		 *
		 * @return
		 */
		StructuredMutation build();
	}
}
