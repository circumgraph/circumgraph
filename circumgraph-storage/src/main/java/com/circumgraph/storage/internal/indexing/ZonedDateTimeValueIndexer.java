package com.circumgraph.storage.internal.indexing;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class ZonedDateTimeValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "ZONED_DATE_TIME";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.ZONED_DATE_TIME;
	}

	@Override
	public SearchFieldType<ZonedDateTime> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				timestamp -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC),
				object -> object.toInstant().toEpochMilli()
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
