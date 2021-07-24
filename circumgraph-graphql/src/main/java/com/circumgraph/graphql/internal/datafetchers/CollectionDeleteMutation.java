package com.circumgraph.graphql.internal.datafetchers;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.storage.Collection;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.ylem.ids.LongIdCodec;

public class CollectionDeleteMutation
	implements DataFetcher<CompletableFuture<Boolean>>
{
	private final Collection collection;
	private final LongIdCodec<String> idCodec;

	public CollectionDeleteMutation(
		Collection collection,
		LongIdCodec<String> idCodec
	)
	{
		this.collection = collection;
		this.idCodec = idCodec;
	}

	@Override
	public CompletableFuture<Boolean> get(DataFetchingEnvironment environment)
		throws Exception
	{
		// TODO: Input validation for id
		var id = environment.getArgument("id");
		var parsedId = id == null ? 0 : idCodec.decode((String) id);

		StorageContext ctx = environment.getContext();
		return ctx.getTx()
			.wrap(collection.delete(parsedId).map(m -> true))
			.toFuture();
	}
}
