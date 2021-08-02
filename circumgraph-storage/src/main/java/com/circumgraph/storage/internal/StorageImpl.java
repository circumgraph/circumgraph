package com.circumgraph.storage.internal;

import java.nio.file.Path;

import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.StructuredValue;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.ImmutableMap;

import reactor.core.publisher.Mono;
import se.l4.silo.CollectionRef;
import se.l4.silo.StorageException;
import se.l4.silo.Transactions;
import se.l4.silo.engine.CollectionDef;
import se.l4.silo.engine.LocalSilo;

public class StorageImpl
	implements Storage
{
	private final Model model;
	private final LocalSilo silo;
	private final ImmutableMap<String, CollectionImpl> collections;

	public StorageImpl(Model model, LocalSilo silo)
	{
		this.model = model;
		this.silo = silo;

		var providers = new ValueProviders();
		var mappers = new ValueMappers(model, this, providers);
		this.collections = model.getImplements(StorageSchema.ENTITY_NAME)
			.toMap(StructuredDef::getName, def -> new CollectionImpl(
				silo.transactions(),
				def,
				silo.getCollection(CollectionRef.create("collection:" + def.getName(), Long.class, StoredObjectValue.class)),
				mappers.createRoot(def)
			))
			.toImmutable();
	}

	@Override
	public Model getModel()
	{
		return model;
	}

	@Override
	public RichIterable<? extends Collection> getCollections()
	{
		return collections.valuesView();
	}

	@Override
	public Collection get(String id)
	{
		return collections.get(id);
	}

	@Override
	public void close()
	{
		silo.close();
	}

	@Override
	public Transactions transactions()
	{
		return silo.transactions();
	}

	public static RichIterable<CollectionDef<Long, StoredObjectValue>> generateCollectionDefinitions(
		Model model
	)
	{
		ValueIndexers indexing = new ValueIndexers();
		ValueSerializers serializers = new ValueSerializers(model);
		return model.getImplements(StorageSchema.ENTITY_NAME)
			.collect(def -> {
				var codec = new ObjectCodecImpl(
					serializers.resolvePolymorphic(
						def,
						Lists.immutable.of(def),
						false
					)
				);

				return CollectionDef.create(StoredObjectValue.class, "collection:" + def.getName())
					.withId(Long.class, StorageImpl::getID)
					.withCodec(codec)
					.addIndex(indexing.generateDefinition(def))
					.build();
			});
	}

	public static Long getID(StructuredValue value)
	{
		SimpleValue id = (SimpleValue) value.getFields().get("id");
		if(id == null)
		{
			throw new StorageException("No identifier present");
		}

		return (Long) id.get();
	}

	public static Builder open(Model model, Path path)
	{
		return new BuilderImpl(model, path);
	}

	public static class BuilderImpl
		implements Builder
	{
		private final Model model;
		private final Path path;

		public BuilderImpl(
			Model model,
			Path path
		)
		{
			this.model = model;
			this.path = path;
		}

		@Override
		public Mono<Storage> start()
		{
			return Mono.fromSupplier(() -> StorageImpl.generateCollectionDefinitions(model))
				.flatMap(defs -> LocalSilo.open(path)
					.addCollections(defs)
					.start()
				)
				.map(silo -> new StorageImpl(model, silo));
		}
	}
}
