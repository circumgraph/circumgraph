package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * Indexer for token values.
 */
public class TokenStringValueIndexer
	implements ValueIndexer
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
		return SearchFieldType.forString()
			.token()
			.build();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		return getClass() == obj.getClass();
	}
}
