package com.circumgraph.graphql.internal.datafetchers;

import com.circumgraph.graphql.FieldResolver;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link DataFetcher} that adapts a {@link FieldResolver}. This is simply
 * used to add some extra sugar that resolves {@link Mono} and {@link Flux}
 * into futures.
 */
public class FieldResolverAdapter
	implements DataFetcher<Object>
{
	private final FieldResolver resolver;

	public FieldResolverAdapter(FieldResolver resolver)
	{
		this.resolver = resolver;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Object get(DataFetchingEnvironment environment)
		throws Exception
	{
		var object = resolver.resolve(environment);
		if(object instanceof Mono)
		{
			return ((Mono) object).toFuture();
		}
		else if(object instanceof Flux)
		{
			return ((Flux) object).collectList().toFuture();
		}

		return object;
	}
}
