package com.circumgraph.graphql.internal.datafetchers;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.silo.Transaction;

public class StructuredValueDataFetcher<I extends Value, O>
	implements DataFetcher<Object>
{
	private final String key;
	private final OutputMapper<I, O> mapper;

	public StructuredValueDataFetcher(
		String key,
		OutputMapper<I, O> mapper
	)
	{
		this.key = key;
		this.mapper = mapper;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object get(DataFetchingEnvironment environment)
		throws Exception
	{
		Object source = environment.getSource();
		StorageContext ctx = environment.getContext();

		if(source instanceof StructuredValue)
		{
			return getAndMap(ctx.getTx(), (StructuredValue) source);
		}

		return ((CompletableFuture) source)
			.thenApply(v -> getAndMap(ctx.getTx(), (StructuredValue) v));
	}

	private Object getAndMap(Transaction tx, StructuredValue v)
	{
		var value = v.getField(key);
		if(value.isEmpty())
		{
			// No value, return null
			return null;
		}

		return ((OutputMapper) mapper).toOutput(tx, value.get());
	}
}
