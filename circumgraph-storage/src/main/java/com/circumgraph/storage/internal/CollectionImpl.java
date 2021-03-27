package com.circumgraph.storage.internal;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageException;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.internal.search.QueryImpl;
import com.circumgraph.storage.internal.search.SearchResultImpl;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.mutation.StructuredMutation.Builder;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.SearchResult;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import reactor.core.publisher.Mono;
import se.l4.silo.StoreResult;
import se.l4.ylem.ids.LongIdGenerator;

public class CollectionImpl
	implements Collection
{
	private final StructuredDef definition;

	private final se.l4.silo.Collection<Long, StoredObjectValue> backing;
	private final LongIdGenerator ids;

	private final ValueMapper<StoredObjectValue, StructuredMutation> mapper;

	public CollectionImpl(
		LongIdGenerator ids,
		StructuredDef definition,
		se.l4.silo.Collection<Long, StoredObjectValue> backing,
		ValueMapper<StoredObjectValue, StructuredMutation> mapper
	)
	{
		this.ids = ids;
		this.definition = definition;
		this.backing = backing;
		this.mapper = mapper;
	}

	@Override
	public StructuredDef getDefinition()
	{
		return definition;
	}

	@Override
	public Mono<Boolean> contains(long id)
	{
		return backing.contains(id);
	}

	@Override
	public Mono<StoredObjectValue> get(long id)
	{
		return backing.get(id)
			.onErrorMap(se.l4.silo.StorageException.class, e -> new StorageException("Unable to read from storage; " + e.getMessage(), e));
	}

	@Override
	public StructuredMutation.Builder newMutation()
	{
		return StructuredMutation.create(definition);
	}

	@Override
	public Builder newMutation(StructuredDef subType)
	{
		// TODO: Verify that this is actually a sub-type
		return StructuredMutation.create(subType);
	}

	@Override
	public Mono<StoredObjectValue> store(StructuredMutation mutation)
	{
		return Mono.fromSupplier(() -> new StoredObjectValueImpl(StructuredValue.create(mutation.getType())
			.add("id", SimpleValue.create(ScalarDef.ID, ids.next()))
			.build()
		))
			.flatMap(v -> store(v, mutation));
	}

	@Override
	public Mono<StoredObjectValue> store(long id, StructuredMutation mutation)
	{
		// TODO: If the type changes this should keep the id
		return backing.get(id)
			.flatMap(v -> store(v, mutation));
	}

	private Mono<StoredObjectValue> store(
		StoredObjectValue current,
		StructuredMutation mutation
	)
	{
		// TODO: This should validate before it stores
		// TODO: Do we need to do a reverse mapping of the result?
		return Mono.fromSupplier(() -> mapper.applyMutation(current, mutation))
			.flatMap(backing::store)
			.map(StoreResult::getData)
			.onErrorMap(se.l4.silo.StorageException.class, e -> new StorageException("Unable to store; " + e.getMessage(), e));
	}

	@Override
	public Mono<Void> delete(long id)
	{
		return backing.delete(id).then();
	}

	@Override
	public Mono<SearchResult> search(Query query)
	{
		return backing.fetch(((QueryImpl) query).buildQuery())
			.map(sr -> new SearchResultImpl(sr));
	}
}
