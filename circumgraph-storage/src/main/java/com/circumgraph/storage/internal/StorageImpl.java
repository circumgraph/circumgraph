package com.circumgraph.storage.internal;

import java.nio.file.Path;

import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Entity;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;

import reactor.core.publisher.Mono;
import se.l4.silo.CollectionRef;
import se.l4.silo.StorageException;
import se.l4.silo.engine.CollectionDef;
import se.l4.silo.engine.LocalSilo;
import se.l4.ylem.ids.LongIdGenerator;
import se.l4.ylem.ids.SequenceLongIdGenerator;

public class StorageImpl
	implements Storage
{
	private final Model model;
	private final LocalSilo silo;
	private final ImmutableMap<String, EntityImpl> entities;

	public StorageImpl(Model model, LocalSilo silo)
	{
		this.model = model;
		this.silo = silo;

		LongIdGenerator ids = new SequenceLongIdGenerator();

		EntityMappers mappers = new EntityMappers(model);
		this.entities = model.getImplements("Entity")
			.toMap(StructuredDef::getName, def -> new EntityImpl(
				ids,
				def,
				silo.getCollection(CollectionRef.create("entity:" + def.getName(), Long.class, StoredEntityValue.class)),
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
	public RichIterable<? extends Entity> getEntities()
	{
		return entities.valuesView();
	}

	@Override
	public Entity get(String id)
	{
		return entities.get(id);
	}

	@Override
	public void close()
	{
		silo.close();
	}

	public static RichIterable<CollectionDef<Long, StoredEntityValue>> generateEntityDefinitions(
		Model model
	)
	{
		EntityIndexing indexing = new EntityIndexing();
		EntitySerializers serializers = new EntitySerializers(model);
		return model.getImplements("Entity")
			.collect(def -> {
				var codec = new ObjectCodecImpl(
					serializers.resolvePolymorphic(def)
				);

				return CollectionDef.create(StoredEntityValue.class, "entity:" + def.getName())
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
			return Mono.fromSupplier(() -> StorageImpl.generateEntityDefinitions(model))
				.flatMap(defs -> LocalSilo.open(path)
					.addCollections(defs)
					.start()
				)
				.map(silo -> new StorageImpl(model, silo));
		}
	}
}
