package com.circumgraph.storage.internal.indexing;

import java.time.LocalDate;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class LocalDateValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "LOCAL_DATE";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.LOCAL_DATE;
	}

	@Override
	public SearchFieldType<LocalDate> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				epochDay -> LocalDate.ofEpochDay(epochDay),
				object -> object.toEpochDay()
			)
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
