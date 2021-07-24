package com.circumgraph.graphql.internal.datafetchers;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

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
		Value value = v.getFields().get(key);
		return ((OutputMapper) mapper).toOutput(value);
	}
}
