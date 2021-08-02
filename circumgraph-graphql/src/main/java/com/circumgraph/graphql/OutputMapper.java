package com.circumgraph.graphql;

import com.circumgraph.storage.Value;

import graphql.schema.GraphQLOutputType;
import se.l4.silo.Transaction;

/**
 * Mapper for output.
 */
public interface OutputMapper<I extends Value, O>
{
	/**
	 * Get the GraphQL type.
	 *
	 * @return
	 */
	GraphQLOutputType getGraphQLType();

	/**
	 * Convert the value into GraphQL output.
	 *
	 * @param in
	 * @return
	 */
	O toOutput(Transaction tx, I in);
}
