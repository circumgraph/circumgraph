package com.circumgraph.storage.mutation;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.internal.mutation.StoredObjectRefMutationImpl;

/**
 * Mutation of a reference to an object.
 */
public interface StoredObjectRefMutation
	extends TypedMutation
{
	/**
	 * Get the type of object being referenced.
	 */
	StructuredDef getDef();

	/**
	 * The identifier of the object being referenced.
	 *
	 * @return
	 */
	long getId();

	static StoredObjectRefMutation create(StructuredDef type, long id)
	{
		return new StoredObjectRefMutationImpl(type, id);
	}

	static StoredObjectRefMutation empty()
	{
		return new StoredObjectRefMutationImpl(null, 0);
	}
}
