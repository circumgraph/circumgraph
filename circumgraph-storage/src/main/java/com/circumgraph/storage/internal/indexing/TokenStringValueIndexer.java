package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.SearchFields;
import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Indexer for token values.
 */
public class TokenStringValueIndexer
	implements ValueIndexer<String>
{
	@Override
	public String getName()
	{
		return "TOKEN";
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
			.token()
			.build();
	}
}
