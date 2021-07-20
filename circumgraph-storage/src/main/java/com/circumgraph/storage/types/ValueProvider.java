package com.circumgraph.storage.types;

import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.values.Value;

import reactor.core.publisher.Mono;

/**
 * Provider of a {@link Value} used to implement things such as default values.
 */
public interface ValueProvider<V extends Value>
{
	/**
	 * Get the definition this provider generates values for.
	 *
	 * @return
	 */
	SimpleValueDef getType();

	/**
	 * Create the value.
	 *
	 * @return
	 */
	Mono<V> create();
}
