package com.circumgraph.graphql;

/**
 * Factory used to resolve instances of {@link FieldResolver}.
 */
public interface FieldResolverFactory
{
	/**
	 * Create the {@link FieldResolver} to use.
	 *
	 * @return
	 */
	FieldResolver create(GraphQLCreationEncounter encounter);
}
