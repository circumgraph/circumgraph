package com.circumgraph.graphql.internal.output;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StoredObjectValue;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import se.l4.silo.Transaction;

public class StoredObjectRefOutputMapper
	implements OutputMapper<StoredObjectRef, CompletableFuture<StoredObjectValue>>
{
	private final Collection collection;
	private final GraphQLTypeReference type;

	public StoredObjectRefOutputMapper(
		Collection collection
	)
	{
		this.collection = collection;
		this.type = GraphQLTypeReference.typeRef(collection.getDefinition().getName());
	}

	@Override
	public GraphQLOutputType getGraphQLType()
	{
		return type;
	}

	@Override
	public CompletableFuture<StoredObjectValue> toOutput(
		Transaction tx,
		StoredObjectRef in
	)
	{
		return tx.wrap(collection.get(in.getId())).toFuture();
	}
}
