package com.circumgraph.storage.internal;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.search.QueryPath;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.MutableSet;

import reactor.core.publisher.Mono;
import se.l4.silo.CollectionRef;
import se.l4.silo.StorageException;
import se.l4.silo.Transactions;
import se.l4.silo.engine.CollectionDef;
import se.l4.silo.engine.LocalSilo;
import se.l4.silo.engine.index.search.SearchFieldDef;
import se.l4.silo.engine.index.search.SearchIndexDef;
import se.l4.silo.engine.index.search.types.SearchFieldType;

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
		this.collections = model.get(StorageSchema.ENTITY_NAME, InterfaceDef.class)
			.get()
			.getImplementors()
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
		ValueSerializers serializers = new ValueSerializers(model);
		return model.get(StorageSchema.ENTITY_NAME, InterfaceDef.class)
			.get()
			.getImplementors()
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
					.addIndex(generateIndexDef(model, def))
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

	/**
	 * Take a {@link StructuredDef} and generate a {@link SearchIndexDef} that
	 * is passed to Silo.
	 *
	 * @param model
	 * @param def
	 * @return
	 */
	public static SearchIndexDef<StoredObjectValue> generateIndexDef(
		Model model,
		StructuredDef def
	)
	{
		var fields = Lists.mutable.<SearchFieldDef<StoredObjectValue>>empty();

		ValueGenerator gen = (root, consumer) -> consumer.accept(root);

		var rootPath = QueryPath.root(def);

		collectIndexedFields(
			model,

			null,
			def,
			rootPath,
			false,
			gen,

			fields::add
		);

		return SearchIndexDef.create(StoredObjectValue.class, "main")
			.addFields(fields)
			.build();
	}

	/**
	 * Collect indexed fields by recursively visiting types and fields.
	 *
	 * @param model
	 * @param field
	 * @param def
	 * @param path
	 * @param multiple
	 * @param generator
	 * @param fieldReceiver
	 */
	private static void collectIndexedFields(
		Model model,

		FieldDef field,
		TypeDef def,
		QueryPath path,
		boolean multiple,
		ValueGenerator generator,

		Consumer<SearchFieldDef<StoredObjectValue>> fieldReceiver
	)
	{
		if(field != null && ! StorageModel.isIndexed(field))
		{
			// Field has not been marked as indexed
			return;
		}

		if(def instanceof NonNullDef.Output)
		{
			def = ((NonNullDef.Output) def).getType();
		}

		if(def instanceof ListDef)
		{
			var listDef = (ListDef) def;
			collectIndexedFields(
				model,

				field,
				listDef.getItemType(),
				path,
				true,

				(root, consumer) -> generator.generate(root, v -> {
					var listValue = (ListValue) v;
					listValue.items().forEach(consumer);
				}),

				fieldReceiver
			);
		}
		else if(def instanceof SimpleValueDef)
		{
			var indexer = StorageModel.getIndexer(field);
			var sortable = StorageModel.isSortable(field);
			var highlightable = StorageModel.isHighlightable(field);

			// Only handle this field if it is indexed
			if(indexer.isEmpty()) return;

			if(multiple)
			{
				var searchField = SearchFieldDef.create(StoredObjectValue.class, path.toIndexName())
					.withType(indexer.get().getSearchFieldType())
					.withHighlighting(highlightable)
					.collection()
					.withSupplier(value -> {
						var list = Lists.mutable.empty();
						generator.generate(value, v -> list.add(extractValue(v)));
						return (Iterable) list;
					})
					.build();

				fieldReceiver.accept(searchField);
			}
			else
			{
				var searchField = SearchFieldDef.create(StoredObjectValue.class, path.toIndexName())
					.withType((SearchFieldType<Object>) indexer.get().getSearchFieldType())
					.withHighlighting(highlightable)
					.withSupplier(value -> {
						var list = Lists.mutable.empty();
						generator.generate(value, v -> list.add(extractValue(v)));
						return (Object) list.getFirst();
					})
					.withSortable(sortable)
					.build();

				fieldReceiver.accept(searchField);
			}
		}
		else if(def instanceof StructuredDef)
		{
			/*
			 * Three cases, either:
			 *
			 * 1) Link to another collection - handle as ID type
			 * 2) InterfaceDef
			 * 3) ObjectDef
			 */
			var structuredDef = (StructuredDef) def;

			if(! path.isRoot() && structuredDef.findImplements(StorageSchema.ENTITY_NAME))
			{
				// Not root entity and implements entity - index as an ID
				collectIndexedFields(
					model,

					field,
					ScalarDef.ID,
					path,
					multiple,
					generator,

					fieldReceiver
				);
			}
			else if(def instanceof InterfaceDef i)
			{
				// Index type of object - this is used for any/null queries
				fieldReceiver.accept(createTypenameField(
					generator,
					path,
					structuredDef
				));

				/*
				 * Go through all the implementations of this interface and
				 * index all the fields in it.
				 */
				MutableSet<String> fields = Sets.mutable.empty();
				for(var subDef : i.getAllImplementors())
				{
					// Index type of object - this is used for any/null queries
					var specificPath = path.polymorphic(subDef);
					if(fields.add(specificPath.toIndexName()))
					{
						fieldReceiver.accept(createTypenameField(
							generator,
							specificPath,
							subDef
						));
					}

					for(var fieldDef : subDef.getFields())
					{
						var name = fieldDef.getName();
						var fieldPath = path
							.polymorphic(fieldDef.getDeclaringType())
							.field(name);

						if(! fields.add(fieldPath.toIndexName())) continue;

						ValueGenerator fieldGenerator = createFieldGenerator(generator, name);

						collectIndexedFields(
							model,

							fieldDef,
							fieldDef.getType(),
							fieldPath,
							multiple,
							fieldGenerator,

							fieldReceiver
						);
					}
				}
			}
			else
			{
				// Index type of object - this is used for any/null queries
				fieldReceiver.accept(createTypenameField(
					generator,
					path.polymorphic(structuredDef),
					structuredDef
				));

				for(var fieldDef : structuredDef.getFields())
				{
					var name = fieldDef.getName();
					ValueGenerator fieldGenerator = createFieldGenerator(generator, name);

					collectIndexedFields(
						model,

						fieldDef,
						fieldDef.getType(),
						path.field(name),
						multiple,
						fieldGenerator,

						fieldReceiver
					);
				}
			}
		}
	}

	private static SearchFieldDef<StoredObjectValue> createTypenameField(
		ValueGenerator generator,
		QueryPath path,
		StructuredDef def
	)
	{
		return SearchFieldDef.create(StoredObjectValue.class, path.typename().toIndexName())
			.withType(SearchFieldType.forString().token().build())
			.withSupplier(value -> {
				if(value == null) return null;

				var list = Lists.mutable.<String>empty();
				generator.generate(value, v -> {
					if(def.isAssignableFrom(v.getDefinition()))
					{
						list.add(v.getDefinition().getName());
					}
				});
				return (String) list.getFirst();
			})
			.build();
	}

	private static Object extractValue(Value v)
	{
		if(v == null) return null;

		if(v instanceof SimpleValue)
		{
			return ((SimpleValue) v).get();
		}
		else if(v instanceof StoredObjectRef)
		{
			return ((StoredObjectRef) v).getId();
		}

		return null;
	}

	interface ValueGenerator
	{
		void generate(Value root, Consumer<Value> consumer);
	}

	private static ValueGenerator createFieldGenerator(ValueGenerator generator, String name)
	{
		return (root, consumer) -> generator.generate(
			root,
			v -> {
				var value = ((StructuredValue) v).getFields().get(name);
				if(value != null)
				{
					consumer.accept(value);
				}
			}
		);
	}
}
