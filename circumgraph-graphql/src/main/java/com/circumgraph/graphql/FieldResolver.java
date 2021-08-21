package com.circumgraph.graphql;

import com.circumgraph.model.FieldDef;
import com.circumgraph.storage.Value;

import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Mono;

/**
 * Resolver for the value of a {@link FieldDef}. Set via the metadata key
 * {@link GraphQLAPISchema#FIELD_RESOLVER} or via a
 * {@link FieldResolverFactory}.
 */
public interface FieldResolver
{
	/**
	 * Resolve the value.
	 *
	 * @param env
	 * @return
	 *   {@link Value}, {@link Mono} or {@link Iterable}
	 */
	Object resolve(DataFetchingEnvironment env);

	/**
	 * Check if another object equals this resolver. Should be implemented to
	 * avoid looping during schema processing.
	 *
	 * @param obj
	 * @return
	 */
	@Override
	boolean equals(Object obj);
}
