package com.circumgraph.storage;

import java.nio.file.Path;

import com.circumgraph.model.Model;
import com.circumgraph.storage.internal.StorageImpl;

import org.eclipse.collections.api.RichIterable;

import reactor.core.publisher.Mono;

/**
 * Storage
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
	 * Get all of the entities available in the system.
	 */
	RichIterable<? extends Entity> getEntities();

	/**
	 * Get an entity based on its identifier.
	 *
	 * @param id
	 * @return
	 */
	Entity get(String id);

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
