package com.circumgraph.storage.types;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.providers.StaticValueProvider;

import reactor.core.publisher.Mono;

/**
 * Provider of a {@link Value} used to implement things such as default values.
 */
public interface ValueProvider
{
	/**
	 * Get the definition this provider generates values for.
	 *
	 * @return
	 */
	OutputTypeDef getType();

	/**
	 * Create the value.
	 *
	 * @return
	 */
	Mono<Value> create();

	/**
	 * Create a provider for a static value.
	 *
	 * @param value
	 * @return
	 */
	static ValueProvider createStatic(Value value)
	{
		return new StaticValueProvider(value);
	}
}
