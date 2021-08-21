package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * {@link ValueIndexer} for {@link EnumDef enums}.
 */
public class EnumValueIndexer
	implements ValueIndexer
{
	private final EnumDef def;

	public EnumValueIndexer(EnumDef def)
	{
		this.def = def;
	}

	@Override
	public String getName()
	{
		return def.getName();
	}

	@Override
	public SimpleValueDef getType()
	{
		return def;
	}

	@Override
	public SearchFieldType<String> getSearchFieldType()
	{
		return SearchFieldType.forString().token().build();
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
