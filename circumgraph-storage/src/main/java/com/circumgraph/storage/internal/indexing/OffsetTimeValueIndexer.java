package com.circumgraph.storage.internal.indexing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class OffsetTimeValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "OFFSET_TIME";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.OFFSET_TIME;
	}

	@Override
	public SearchFieldType<OffsetTime> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				timestamp -> OffsetTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC),
				object -> object.atDate(LocalDate.EPOCH).toInstant().toEpochMilli()
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
