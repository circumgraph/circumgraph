package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class FloatValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "FLOAT";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.FLOAT;
	}

	@Override
	public SearchFieldType<Double> getSearchFieldType()
	{
		return SearchFieldType.forDouble().build();
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
