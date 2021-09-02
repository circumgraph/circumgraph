package com.circumgraph.storage.internal;

import java.util.Optional;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.internal.indexing.BooleanValueIndexer;
import com.circumgraph.storage.internal.indexing.EnumValueIndexer;
import com.circumgraph.storage.internal.indexing.FloatValueIndexer;
import com.circumgraph.storage.internal.indexing.FullTextStringValueIndexer;
import com.circumgraph.storage.internal.indexing.IdValueIndexer;
import com.circumgraph.storage.internal.indexing.IntValueIndexer;
import com.circumgraph.storage.internal.indexing.LocalDateTimeValueIndexer;
import com.circumgraph.storage.internal.indexing.LocalDateValueIndexer;
import com.circumgraph.storage.internal.indexing.LocalTimeValueIndexer;
import com.circumgraph.storage.internal.indexing.TokenStringValueIndexer;
import com.circumgraph.storage.internal.indexing.TypeAheadStringValueIndexer;
import com.circumgraph.storage.types.ValueIndexer;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

/**
 * Helper for indexing of objects.
 */
public class ValueIndexers
{
	private final ImmutableMap<String, ValueIndexer> indexersByName;
	private final ImmutableMultimap<SimpleValueDef, ValueIndexer> indexersByType;

	public ValueIndexers()
	{
		var indexers = Sets.mutable.<ValueIndexer>of(
			new TokenStringValueIndexer(),
			new FullTextStringValueIndexer(),
			new TypeAheadStringValueIndexer(),
			new FloatValueIndexer(),
			new IntValueIndexer(),
			new IdValueIndexer(),
			new BooleanValueIndexer(),
			new LocalDateValueIndexer(),
			new LocalTimeValueIndexer(),
			new LocalDateTimeValueIndexer()
		);

		MutableMap<String, ValueIndexer> indexersByName = Maps.mutable.empty();
		MutableMultimap<SimpleValueDef, ValueIndexer> indexersByType = Multimaps.mutable.set.empty();

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
	 * @param name
	 * @return
	 */
	public Optional<ValueIndexer> getIndexer(String name)
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
	public Optional<ValueIndexer> guessBestIndexer(SimpleValueDef value)
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

	public RichIterable<String> getSupportedIndexers(SimpleValueDef def)
	{
		return indexersByType.get(def).collect(d -> d.getName());
	}
}
