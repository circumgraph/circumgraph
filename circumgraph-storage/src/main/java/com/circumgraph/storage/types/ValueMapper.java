package com.circumgraph.storage.types;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.mappers.MappingEncounter;
import com.circumgraph.storage.mutation.Mutation;

import reactor.core.publisher.Mono;

/**
 * Mapper for a value in a {@link StructuredDef} or {@link ListDef} object.
 */
public interface ValueMapper<V extends Value, M extends Mutation>
{
	/**
	 * The definition this is mapping for.
	 *
	 * @return
	 */
	OutputTypeDef getDef();

	/**
	 * Apply the given mutation to a previous value.
	 *
	 * @param encounter
	 *   encounter used to report errors and perform special functions
	 * @param location
	 *   the location of the value being mapped, should be passed to
	 *   {@link ValidationMessage.Builder#withLocation(Location)}
	 *   if a validation error is encountered
	 * @param previousValue
	 *   the previous value, or {@code null}
	 * @param mutation
	 *   mutation to apply
	 * @return
	 *   mapped value
	 */
	Mono<V> applyMutation(
		MappingEncounter encounter,
		com.circumgraph.model.ObjectLocation location,
		V previousValue,
		M mutation
	);
}
