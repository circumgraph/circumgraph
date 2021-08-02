package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;

/**
 * Encounter used when applying {@link Mutation}s in a {@link ValueMapper}.
 */
public interface MappingEncounter
{
	/**
	 * Report a validation error.
	 *
	 * @param message
	 */
	void reportError(ValidationMessage message);

	/**
	 * Register a link between the current object and another object.
	 *
	 * @param collection
	 * @param object
	 */
	void link(String collection, long object);

	/**
	 * Remove a link between the current object and another object.
	 *
	 * @param collection
	 * @param object
	 */
	void unlink(String collection, long object);

	/**
	 * Externalize the given value. This will move it into a separate area
	 * where its value will only be loaded on demand.
	 *
	 * @param value
	 */
	Value externalize(Value value);

	/**
	 * Remove an external value.
	 *
	 * @param value
	 */
	void removeExternal(Value value);
}
