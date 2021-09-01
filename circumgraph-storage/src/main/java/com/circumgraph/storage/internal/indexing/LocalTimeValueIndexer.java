package com.circumgraph.storage.internal.indexing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class LocalTimeValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "LOCAL_TIME";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.LOCAL_TIME;
	}

	@Override
	public SearchFieldType<LocalTime> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				now -> LocalTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC),
				object -> {
					var instantAtEpoch = Instant.ofEpochSecond(
						object.toEpochSecond(LocalDate.EPOCH, ZoneOffset.UTC),
						object.getNano()
					);
					return instantAtEpoch.toEpochMilli();
				}
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
