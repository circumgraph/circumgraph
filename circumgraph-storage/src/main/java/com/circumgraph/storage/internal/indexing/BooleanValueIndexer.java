package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class BooleanValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "BOOLEAN";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.BOOLEAN;
	}

	@Override
	public SearchFieldType<Boolean> getSearchFieldType()
	{
		return SearchFieldType.forBoolean().build();
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

