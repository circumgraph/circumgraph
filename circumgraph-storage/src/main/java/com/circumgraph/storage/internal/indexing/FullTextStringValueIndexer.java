package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.SearchFields;
import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Indexer for full-text string values.
 */
public class FullTextStringValueIndexer
	implements ValueIndexer<String>
{
	@Override
	public String getName()
	{
		return "FULL_TEXT";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.STRING;
	}

	@Override
	public SearchFieldType<String> getSearchFieldType()
	{
		return SearchFields.string()
			.fullText()
			.build();
	}
}
