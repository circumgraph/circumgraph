package com.circumgraph.graphql.internal.resolvers;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;

import graphql.schema.DataFetchingEnvironment;

/**
 * {@link FieldResolver} that is used for getting a value that is stored.
 */
public class StoredValueFieldResolver<I extends Value, O>
	implements FieldResolver
{
	private final String key;
	private final OutputMapper<I> mapper;

	public StoredValueFieldResolver(
		String key,
		OutputMapper<I> mapper
	)
	{
		this.key = key;
		this.mapper = mapper;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object resolve(DataFetchingEnvironment env)
	{
		StructuredValue source = env.getSource();
		StorageContext ctx = env.getContext();

		var value = source.getField(key);
		if(value.isEmpty())
		{
			// No value, return null
			return null;
		}

		return ((OutputMapper) mapper).toOutput(ctx.getTx(), value.get());
	}
}
