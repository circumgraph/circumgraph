package com.circumgraph.storage.internal;

import java.util.Optional;
import java.util.function.Consumer;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.indexing.BooleanValueIndexer;
import com.circumgraph.storage.internal.indexing.EnumValueIndexer;
import com.circumgraph.storage.internal.indexing.FloatValueIndexer;
import com.circumgraph.storage.internal.indexing.FullTextStringValueIndexer;
import com.circumgraph.storage.internal.indexing.IdValueIndexer;
import com.circumgraph.storage.internal.indexing.IntValueIndexer;
import com.circumgraph.storage.internal.indexing.TokenStringValueIndexer;
import com.circumgraph.storage.internal.indexing.TypeAheadStringValueIndexer;
import com.circumgraph.storage.types.ValueIndexer;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import se.l4.silo.engine.index.search.SearchFieldDef;
import se.l4.silo.engine.index.search.SearchIndexDef;
import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Helper for indexing of objects.
 */
public class ValueIndexers
{
	private final ImmutableMap<String, ValueIndexer<?>> indexersByName;
	private final ImmutableMultimap<SimpleValueDef, ValueIndexer<?>> indexersByType;

	public ValueIndexers()
	{
		var indexers = Sets.mutable.<ValueIndexer<?>>of(
			new TokenStringValueIndexer(),
			new FullTextStringValueIndexer(),
			new TypeAheadStringValueIndexer(),
			new FloatValueIndexer(),
			new IntValueIndexer(),
			new IdValueIndexer(),
			new BooleanValueIndexer()
		);

		MutableMap<String, ValueIndexer<?>> indexersByName = Maps.mutable.empty();
		MutableMultimap<SimpleValueDef, ValueIndexer<?>> indexersByType = Multimaps.mutable.set.empty();

		for(var indexer : indexers)
		{
			indexersByName.put(indexer.getName(), indexer);
			indexersByType.put(indexer.getType(), indexer);
		}

		this.indexersByName = indexersByName.toImmutable();
		this.indexersByType = indexersByType.toImmutable();
	}

	/**
	 * Get indexer used for the given name.
	 *
	 * @param def
	 * @return
	 */
	public Optional<ValueIndexer<?>> getIndexer(String name)
	{
		return Optional.ofNullable(indexersByName.get(name));
	}

	/**
	 * Guess the best indexer to use for the given type. This will return
	 * an indexer if there's a well known one or if there is only a single
	 * type registered.
	 *
	 * @param value
	 * @return
	 */
	public Optional<ValueIndexer<?>> guessBestIndexer(SimpleValueDef value)
	{
		switch(value.getName())
		{
			case "String":
				return getIndexer("FULL_TEXT");
		}

		var indexers = indexersByType.get(value);
		if(indexers.size() == 1)
		{
			return Optional.of(indexers.getAny());
		}

		if(value instanceof EnumDef)
		{
			// Enums are automatically resolved

			// TODO: Caching of instances?
			var indexer = new EnumValueIndexer((EnumDef) value);
			return Optional.of(indexer);
		}

		return Optional.empty();
	}

	public boolean hasMultipleIndexers(SimpleValueDef def)
	{
		return indexersByType.get(def).size() > 1;
	}

	public SearchIndexDef<StoredObjectValue> generateDefinition(
		StructuredDef def
	)
	{
		var fields = Lists.mutable.<SearchFieldDef<StoredObjectValue>>empty();

		ValueGenerator gen = (root, consumer) -> consumer.accept(root);
		collectFields(
			null,
			def,
			null,
			false,
			gen,
			fields::add
		);

		return SearchIndexDef.create(StoredObjectValue.class, "main")
			.addFields(fields)
			.build();
	}

	private void collectFields(
		FieldDef field,
		TypeDef def,
		String path,
		boolean multiple,
		ValueGenerator generator,

		Consumer<SearchFieldDef<StoredObjectValue>> fieldReceiver
	)
	{
		if(def instanceof NonNullDef.Output)
		{
			def = ((NonNullDef.Output) def).getType();
		}

		if(def instanceof ListDef)
		{
			var listDef = (ListDef) def;
			collectFields(
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
			var indexerType = StorageModel.getIndexerType(field);
			var sortable = StorageModel.isSortable(field);
			var highlightable = StorageModel.isHighlightable(field);

			// Only handle this field if it is indexed
			if(indexerType.isEmpty()) return;

			var indexer = getIndexer(indexerType.get()).get();

			if(multiple)
			{
				var searchField = SearchFieldDef.create(StoredObjectValue.class, path)
					.withType(indexer.getSearchFieldType())
					.collection()
					.withSupplier(value -> {
						var list = Lists.mutable.empty();
						generator.generate(value, v -> list.add(extractValue(v)));
						return (Iterable) list;
					})
					.withHighlighting(highlightable)
					.build();

				fieldReceiver.accept(searchField);
			}
			else
			{
				var searchField = SearchFieldDef.create(StoredObjectValue.class, path)
					.withType((SearchFieldType<Object>) indexer.getSearchFieldType())
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

			if(path != null && structuredDef.findImplements(StorageSchema.ENTITY_NAME))
			{
				// Not root entity and implements entity - index as an ID
				collectFields(
					field,
					ScalarDef.ID,
					path,
					multiple,
					generator,

					fieldReceiver
				);
			}
			else if(def instanceof InterfaceDef)
			{
				// For interfaces we first make sure that __typename is available
				var typenameField = SearchFieldDef.create(StoredObjectValue.class, join(path, "__typename"))
					.withType(SearchFieldType.forString().token().build())
					.withSupplier(value -> {
						var list = Lists.mutable.<String>empty();
						generator.generate(value, v -> list.add(v.getDefinition().getName()));
						return (String) list.getFirst();
					})
					.build();

				fieldReceiver.accept(typenameField);

				// Index all of the shared fields
				for(var fieldDef : structuredDef.getFields())
				{
					var name = fieldDef.getName();
					ValueGenerator fieldGenerator = (root, consumer) -> generator.generate(
						root,
						v -> {
							consumer.accept(((StructuredValue) v).getFields().get(name));
						}
					);

					collectFields(
						fieldDef,
						fieldDef.getType(),
						join(join(path, "_"), name),
						multiple,
						fieldGenerator,
						fieldReceiver
					);
				}

				// TODO: Polymorphism
			}
			else
			{
				for(var fieldDef : structuredDef.getFields())
				{
					var name = fieldDef.getName();
					ValueGenerator fieldGenerator = (root, consumer) -> generator.generate(
						root,
						v -> {
							consumer.accept(((StructuredValue) v).getFields().get(name));
						}
					);

					collectFields(
						fieldDef,
						fieldDef.getType(),
						join(join(path, "_"), name),
						multiple,
						fieldGenerator,
						fieldReceiver
					);
				}
			}
		}
	}

	private String join(String path, String next)
	{
		return path == null || path.isEmpty() ? next : path + '.' + next;
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
}
