package com.circumgraph.graphql.internal;

import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.Value;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.l4.silo.Transaction;

/**
 * {@link DataFetcher} that adapts a {@link FieldResolver}. This takes care
 * of mapping {@link Value values}, {@link Mono} instances etc.
 */
public class FieldResolverAdapter
	implements DataFetcher<Object>
{
	private static final Predicate<Object> HAS_FUTURE = o -> o instanceof CompletableFuture<?>;
	private static final CompletableFuture<?>[] EMPTY_FUTURE_ARRAY = new CompletableFuture<?>[0];

	private final Storage storage;
	private final FieldResolver resolver;

	public FieldResolverAdapter(
		Storage storage,
		FieldResolver resolver
	)
	{
		this.storage = storage;
		this.resolver = resolver;
	}

	@Override
	public Object get(DataFetchingEnvironment environment)
		throws Exception
	{
		Transaction tx = environment.getGraphQlContext()
			.get("transaction");
		return map(tx, resolver.resolve(environment));
	}

	private Object map(Transaction tx, Object in)
	{
		if(in instanceof Mono<?> m)
		{
			return tx.wrap(m).map(item -> map(tx, item)).toFuture();
		}
		else if(in instanceof Flux<?> f)
		{
			return tx.wrap(f).map(item -> map(tx, item)).collectList().toFuture();
		}
		else if(in instanceof Iterable<?> rawIt)
		{
			return mapIterable(tx, rawIt);
		}
		else if(in instanceof ListValue<?> lv)
		{
			return map(tx, lv.items());
		}
		else if(in instanceof StoredObjectRef ref)
		{
			return tx.wrap(storage
				.get(ref.getDefinition().getName())
				.get(ref.getId())
			).toFuture();
		}
		else if(in instanceof SimpleValue sv)
		{
			if(sv.getDefinition() == ScalarDef.ID && sv.get() instanceof Long l)
			{
				// Identifiers of type long are from the storage layer - encode these
				return StorageIds.encode(l);
			}

			return sv.get();
		}

		return in;
	}

	/**
	 * Map an iterable. This helps with the case where a list contains
	 * asynchronous value in which case the list must also be converted into
	 * an asynchronous value.
	 *
	 * @param tx
	 * @param rawIt
	 * @return
	 */
	private Object mapIterable(Transaction tx, Iterable<?> rawIt)
	{
		var items = rawIt instanceof ListIterable<?> listIt
			? listIt
			: Lists.immutable.ofAll(rawIt);

		var mappedItems = items.collect(item -> map(tx, item));

		if(mappedItems.anySatisfy(HAS_FUTURE))
		{
			/*
			 * First collect all the values as futures.
			 */
			var futures = mappedItems.collect(item -> {
				return item instanceof CompletableFuture<?> f
					? f
					: CompletableFuture.completedFuture(item);
			});

			/*
			 * Wait for all the futures and resolve the main result when
			 * they are ready.
			 */
			CompletableFuture<Iterable<?>> result = new CompletableFuture<>();
			CompletableFuture.allOf(futures.toArray(EMPTY_FUTURE_ARRAY))
				.whenComplete((ignored, ex) -> {
					if(ex != null)
					{
						result.completeExceptionally(ex);
						return;
					}

					result.complete(futures.collect(CompletableFuture::join));
				});
			return result;
		}
		else
		{
			return mappedItems;
		}
	}
}
