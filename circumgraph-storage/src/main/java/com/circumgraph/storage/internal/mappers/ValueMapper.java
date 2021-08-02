package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;

import reactor.core.publisher.Flux;
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
	 * Get the initial value.
	 */
	Mono<V> getInitialValue();

	/**
	 * Apply the given mutation to a previous value.
	 *
	 * @param encounter
	 *   encounter used to report errors and perform special functions
	 * @param location
	 *   the location of the value being mapped, should be passed to
	 *   {@link ValidationMessage.Builder#withLocation(SourceLocation)}
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
		ObjectLocation location,
		V previousValue,
		M mutation
	);

	/**
	 * Validate a value. Called directly for cases where no mapping is done.
	 *
	 * @param location
	 *   the location of the value being mapped
	 * @param value
	 *   the value, or {@code null}
	 * @return
	 *   flux of validation messages
	 */
	Flux<ValidationMessage> validate(
		ObjectLocation location,
		V value
	);
}
