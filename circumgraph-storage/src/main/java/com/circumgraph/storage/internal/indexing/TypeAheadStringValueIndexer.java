package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Indexer for full text with type ahead search.
 */
public class TypeAheadStringValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "TYPE_AHEAD";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.STRING;
	}

	@Override
	public SearchFieldType<String> getSearchFieldType()
	{
		return SearchFieldType.forString()
			.fullText()
			.withTypeAhead()
			.build();
	}
}
