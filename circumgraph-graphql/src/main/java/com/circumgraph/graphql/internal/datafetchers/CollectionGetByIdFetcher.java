package com.circumgraph.graphql.internal.datafetchers;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.storage.Collection;
import com.circumgraph.values.StructuredValue;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.ylem.ids.LongIdCodec;

/**
 * Fetcher used for getting an object from a {@link Collection} using
 * {@link Collection#get(long)}.
 */
public class CollectionGetByIdFetcher
	implements DataFetcher<CompletableFuture<? extends StructuredValue>>
{
	private final LongIdCodec<String> idCodec;

	public CollectionGetByIdFetcher(LongIdCodec<String> idCodec)
	{
		this.idCodec = idCodec;
	}

	@Override
	public CompletableFuture<? extends StructuredValue> get(DataFetchingEnvironment environment)
		throws Exception
	{
		Collection entity = environment.getSource();
		String id = environment.getArgument("id");
		long numericId = idCodec.decode(id);

		StorageContext ctx = environment.getContext();
		return ctx.getTx()
			.wrap(entity.get(numericId))
			.toFuture();
	}
}
