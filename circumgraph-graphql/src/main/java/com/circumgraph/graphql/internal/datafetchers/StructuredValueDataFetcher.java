package com.circumgraph.graphql.internal.datafetchers;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Mono;

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
		StructuredValue v = environment.getSource();
		var value = v.getField(key);
		if(value.isEmpty())
		{
			// No value, return null
			return null;
		}

		var result = ((OutputMapper) mapper).toOutput(value.get());
		if(result instanceof Mono)
		{
			StorageContext ctx = environment.getContext();
			return ctx.getTx()
				.wrap((Mono) result)
				.toFuture();
		}
		else
		{
			return result;
		}
	}
}
