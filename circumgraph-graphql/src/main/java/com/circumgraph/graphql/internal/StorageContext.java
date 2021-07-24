package com.circumgraph.graphql.internal;

import se.l4.silo.Transaction;

/**
 * Context of a GraphQL invocation.
 */
public class StorageContext
{
	private final Transaction tx;

	public StorageContext(Transaction tx)
	{
		this.tx = tx;
	}

	public Transaction getTx()
	{
		return tx;
	}
}
