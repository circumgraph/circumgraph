package com.circumgraph.storage;

import java.nio.file.Path;

import com.circumgraph.model.Model;
import com.circumgraph.storage.internal.StorageImpl;

import org.eclipse.collections.api.RichIterable;

import reactor.core.publisher.Mono;

/**
 * Storage that contains all the {@link Collection}s created from the active
 * {@link Model}.
 */
public interface Storage
{
	/**
	 * Get the model this storage represents.
	 *
	 * @return
	 */
	Model getModel();

	/**
	 * Get all of the collections available in the system.
	 *
	 * @return
	 *   iterable with all collections
	 */
	RichIterable<? extends Collection> getCollections();

	/**
	 * Get a collection based on its identifier.
	 *
	 * @param id
	 * @return
	 */
	Collection get(String id);

	/**
	 * Close this storage.
	 *
	 * @return
	 */
	void close();

	static Builder open(Model model, Path path)
	{
		return StorageImpl.open(model, path);
	}

	interface Builder
	{
		Mono<Storage> start();
	}
}
