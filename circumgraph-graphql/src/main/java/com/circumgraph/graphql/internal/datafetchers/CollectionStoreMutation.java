package com.circumgraph.graphql.internal.datafetchers;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.mutation.StructuredMutation;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Mono;
import se.l4.ylem.ids.LongIdCodec;

public class CollectionStoreMutation
	implements DataFetcher<CompletableFuture<StoredObjectValue>>
{
	private final Collection collection;
	private final LongIdCodec<String> idCodec;
	private final MutationInputMapper<?> mutationMapper;

	public CollectionStoreMutation(
		Collection collection,
		LongIdCodec<String> idCodec,
		MutationInputMapper<?> mutationMapper
	)
	{
		this.collection = collection;
		this.idCodec = idCodec;
		this.mutationMapper = mutationMapper;
	}

	@Override
	public CompletableFuture<StoredObjectValue> get(DataFetchingEnvironment environment)
		throws Exception
	{
		// TODO: Input validation for id
		var id = environment.getArgument("id");
		var parsedId = id == null ? 0 : idCodec.decode((String) id);
		var mutation =  (StructuredMutation) mutationMapper.toMutation(environment.getArgument("mutation"));

		Mono<StoredObjectValue> store;
		if(parsedId > 0)
		{
			store = collection.store(parsedId, mutation);
		}
		else
		{
			store = collection.store(mutation);
		}

		StorageContext ctx = environment.getContext();
		return ctx.getTx().wrap(store).toFuture();
	}
}
