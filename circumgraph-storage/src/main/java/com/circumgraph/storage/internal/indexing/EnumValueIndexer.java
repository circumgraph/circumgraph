package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * {@link ValueIndexer} for {@link EnumDef enums}.
 */
public class EnumValueIndexer
	implements ValueIndexer<String>
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
}
