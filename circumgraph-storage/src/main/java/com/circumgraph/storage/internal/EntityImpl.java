package com.circumgraph.storage.internal;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Entity;
import com.circumgraph.storage.StorageException;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.mutation.StructuredMutation.Builder;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import reactor.core.publisher.Mono;
import se.l4.silo.StoreResult;
import se.l4.ylem.ids.LongIdGenerator;

public class EntityImpl
	implements Entity
{
	private final StructuredDef definition;

	private final se.l4.silo.Entity<Long, StructuredValue> backing;
	private final LongIdGenerator ids;

	private final ValueMapper<StructuredValue, StructuredMutation> mapper;

	public EntityImpl(
		LongIdGenerator ids,
		StructuredDef definition,
		se.l4.silo.Entity<Long, StructuredValue> backing,
		ValueMapper<StructuredValue, StructuredMutation> mapper
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
	public Mono<StructuredValue> get(long id)
	{
		return backing.get(id)
			.map(m -> {
				// TODO: Resolve references to external data and entities here?
				return m;
			})
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
	public Mono<StructuredValue> store(StructuredMutation mutation)
	{
		return Mono.fromSupplier(() ->  StructuredValue.create(mutation.getType())
			.add("id", SimpleValue.create(ScalarDef.ID, ids.next()))
			.build()
		)
			.flatMap(v -> store(v, mutation));
	}

	@Override
	public Mono<StructuredValue> store(long id, StructuredMutation mutation)
	{
		// TODO: If the type changes this should keep the id
		return backing.get(id)
			.flatMap(v -> store(v, mutation));
	}

	private Mono<StructuredValue> store(
		StructuredValue current,
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
}
