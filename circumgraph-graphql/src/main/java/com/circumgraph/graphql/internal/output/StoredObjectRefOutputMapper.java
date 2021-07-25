package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StoredObjectValue;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import reactor.core.publisher.Mono;

public class StoredObjectRefOutputMapper
	implements OutputMapper<StoredObjectRef, Mono<StoredObjectValue>>
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
	public Mono<StoredObjectValue> toOutput(StoredObjectRef in)
	{
		return collection.get(in.getId());
	}
}
