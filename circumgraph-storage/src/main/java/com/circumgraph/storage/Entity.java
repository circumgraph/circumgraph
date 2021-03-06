package com.circumgraph.storage;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.SearchResult;

import reactor.core.publisher.Mono;

public interface Entity
{
	/**
	 * Get the definition that this entity represents.
	 *
	 * @return
	 */
	StructuredDef getDefinition();

	/**
	 * Start creating a new mutation for the top-level type.
	 *
	 * @return
	 */
	StructuredMutation.Builder newMutation();

	/**
	 * Start creating a new mutation for a sub-type.
	 *
	 * @param subType
	 * @return
	 */
	StructuredMutation.Builder newMutation(StructuredDef subType);

	/**
	 * Get a value based on its identifier.
	 *
	 * @param id
	 * @return
	 */
	Mono<StoredEntityValue> get(long id);

	/**
	 * Create and store an object.
	 *
	 * @param mutation
	 * @return
	 *   the created object
	 */
	Mono<StoredEntityValue> store(StructuredMutation mutation);

	/**
	 * Update an object.
	 *
	 * @param data
	 * @return
	 *   the object with mutations applied
	 */
	Mono<StoredEntityValue> store(long id, StructuredMutation mutation);

	/**
	 * Delete a stored object.
	 *
	 * @param id
	 * @return
	 */
	Mono<Void> delete(long id);

	/**
	 * Execute a specific query.
	 *
	 * @return
	 */
	Mono<SearchResult> search(Query query);
}
