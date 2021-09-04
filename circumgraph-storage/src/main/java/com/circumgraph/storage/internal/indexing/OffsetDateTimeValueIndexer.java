package com.circumgraph.storage.internal.indexing;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class OffsetDateTimeValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "OFFSET_DATE_TIME";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.OFFSET_DATE_TIME;
	}

	@Override
	public SearchFieldType<OffsetDateTime> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				timestamp -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC),
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
