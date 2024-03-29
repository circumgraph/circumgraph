package com.circumgraph.storage.internal;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageException;
import com.circumgraph.storage.StorageSearchException;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.mappers.MappingEncounter;
import com.circumgraph.storage.internal.search.QueryImpl;
import com.circumgraph.storage.internal.search.SearchResultImpl;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.mutation.StructuredMutation.Builder;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.SearchResult;
import com.circumgraph.storage.types.ValueMapper;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import reactor.core.publisher.Mono;
import se.l4.silo.StoreResult;
import se.l4.silo.Transactions;

public class CollectionImpl
	implements Collection
{
	private final Transactions txs;

	private final StructuredDef definition;
	private final se.l4.silo.Collection<Long, StoredObjectValue> backing;
	private final ValueMapper<StoredObjectValue, StructuredMutation> mapper;

	public CollectionImpl(
		Transactions txs,
		StructuredDef definition,
		se.l4.silo.Collection<Long, StoredObjectValue> backing,
		ValueMapper<StoredObjectValue, StructuredMutation> mapper
	)
	{
		this.txs = txs;

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
	public Mono<Boolean> contains(String id)
	{
		return backing.contains(AutoGeneratedIds.decode(id));
	}

	@Override
	public Mono<StoredObjectValue> get(String id)
	{
		return backing.get(AutoGeneratedIds.decode(id))
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
		return txs.transactional(store0(null, mutation));
	}

	@Override
	public Mono<StoredObjectValue> store(String id, StructuredMutation mutation)
	{
		// TODO: If the type changes this should keep the id
		return txs.transactional(backing.get(AutoGeneratedIds.decode(id))
			.flatMap(v -> store0(v, mutation))
		);
	}

	private Mono<StoredObjectValue> store0(
		StoredObjectValue current,
		StructuredMutation mutation
	)
	{
		var encounter = new MappingEncounterImpl();
		return mapper.applyMutation(
			encounter,
			ObjectLocation.root(),
			current,
			mutation
		)
			.doOnNext(v -> {
				if(! encounter.errors.isEmpty())
				{
					throw new StorageValidationException(encounter.errors);
				}
			})
			.flatMap(backing::store)
			.map(StoreResult::getData)
			.onErrorMap(se.l4.silo.StorageException.class, e -> new StorageException("Unable to store; " + e.getMessage(), e));
	}

	@Override
	public Mono<Void> delete(String id)
	{
		return backing.delete(AutoGeneratedIds.decode(id)).then();
	}

	@Override
	public Mono<SearchResult> search(Query query)
	{
		return backing.fetch(((QueryImpl) query).buildQuery())
			.map(sr -> new SearchResultImpl(sr))
			.onErrorMap(se.l4.silo.StorageException.class, e -> new StorageSearchException("Unable to search; " + e.getMessage(), e))
			.cast(SearchResult.class);
	}

	private static class MappingEncounterImpl
		implements MappingEncounter
	{
		private final MutableList<ValidationMessage> errors;

		public MappingEncounterImpl()
		{
			errors = Lists.mutable.empty();
		}

		@Override
		public void reportError(ValidationMessage message)
		{
			if(message.getLevel() == ValidationMessageLevel.ERROR)
			{
				errors.add(message);
			}
		}

		@Override
		public Value externalize(Value value)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeExternal(Value value)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void link(String collection, long object)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void unlink(String collection, long object)
		{
			// TODO Auto-generated method stub

		}
	}
}
