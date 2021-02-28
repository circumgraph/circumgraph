package com.circumgraph.storage.internal;

import java.util.Optional;
import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.internal.indexing.FullTextStringValueIndexer;
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

import se.l4.silo.engine.index.search.SearchFieldDefinition;
import se.l4.silo.engine.index.search.SearchFields;
import se.l4.silo.engine.index.search.SearchIndexDefinition;
import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Helper for indexing of entities.
 */
public class EntityIndexing
{
	private final ImmutableMap<String, ValueIndexer<?>> indexersByName;
	private final ImmutableMultimap<SimpleValueDef, ValueIndexer<?>> indexersByType;

	public EntityIndexing()
	{
		var indexers = Sets.mutable.<ValueIndexer<?>>of(
			new TokenStringValueIndexer(),
			new FullTextStringValueIndexer(),
			new TypeAheadStringValueIndexer()
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
			return Optional.ofNullable(indexers.getAny());
		}

		return Optional.empty();
	}

	public boolean hasMultipleIndexers(SimpleValueDef def)
	{
		return indexersByType.get(def).size() > 1;
	}

	private ValueIndexer<?> getIndexer(String name, SimpleValueDef def)
	{
		var indexer = getIndexer(name);
		if(indexer.isPresent())
		{
			return indexer.get();
		}

		return guessBestIndexer(def).get();
	}

	public SearchIndexDefinition<StructuredValue> generateDefinition(
		StructuredDef def
	)
	{
		var fields = Lists.mutable.<SearchFieldDefinition<StructuredValue>>empty();

		ValueGenerator gen = (root, consumer) -> consumer.accept(root);
		collectFields(
			null,
			def,
			null,
			false,
			gen,
			fields::add
		);

		return SearchIndexDefinition.create(StructuredValue.class, "main")
			.addFields(fields)
			.build();
	}

	private void collectFields(
		FieldDef field,
		TypeDef def,
		String path,
		boolean multiple,
		ValueGenerator generator,

		Consumer<SearchFieldDefinition<StructuredValue>> fieldReceiver
	)
	{
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
			var index = field.getDirective("index");
			var sortable = field.getDirective("sortable").isPresent();
			var highlightable = field.getDirective("highlightable").isPresent();

			// Only handle this field if it is indexed
			if(index.isEmpty()) return;

			var indexer = getIndexer(
				(String) index.get().getArgument("type")
					.map(DirectiveUse.Argument::getValue)
					.orElse(null),
				(SimpleValueDef) def
			);

			if(multiple)
			{
				var searchField = SearchFieldDefinition.create(StructuredValue.class, path)
					.withType(indexer.getSearchFieldType())
					.collection()
					.withSupplier(value -> {
						var list = Lists.mutable.empty();
						generator.generate(value, v -> list.add(((SimpleValue) v).get()));
						return (Iterable) list;
					})
					.withHighlighting(highlightable)
					.build();

				fieldReceiver.accept(searchField);
			}
			else
			{
				var searchField = SearchFieldDefinition.create(StructuredValue.class, path)
					.withType((SearchFieldType<Object>) indexer.getSearchFieldType())
					.withHighlighting(highlightable)
					.withSupplier(value -> {
						var list = Lists.mutable.empty();
						generator.generate(value, v -> list.add(((SimpleValue) v).get()));
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
			 * 1) Link to another entity - handle as ID type
			 * 2) InterfaceDef
			 * 3) ObjectDef
			 */
			var structuredDef = (StructuredDef) def;

			if(path != null && structuredDef.findImplements("Entity"))
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
				// TODO: Indexing of fields in interface implementations

				// For interfaces we first make sure that __typename is available
				var typenameField = SearchFieldDefinition.create(StructuredValue.class, join(path, "__typename"))
					.withType(SearchFields.string().token().build())
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
						v -> ((StructuredValue) v).getFields().get(name)
					);

					collectFields(
						fieldDef,
						fieldDef.getType(),
						join(path, name),
						multiple,
						fieldGenerator,
						fieldReceiver
					);
				}
			}
			else
			{
				for(var fieldDef : structuredDef.getFields())
				{
					var name = fieldDef.getName();
					ValueGenerator fieldGenerator = (root, consumer) -> generator.generate(
						root,
						v -> ((StructuredValue) v).getFields().get(name)
					);

					collectFields(
						fieldDef,
						fieldDef.getType(),
						join(path, name),
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

	interface ValueGenerator
	{
		void generate(Value root, Consumer<Value> consumer);
	}
}
