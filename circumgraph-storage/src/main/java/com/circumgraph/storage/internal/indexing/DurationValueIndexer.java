package com.circumgraph.storage.internal.indexing;

import java.time.Duration;
import java.time.Period;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import org.threeten.extra.PeriodDuration;

import se.l4.silo.engine.index.search.types.SearchFieldType;

public class DurationValueIndexer
	implements ValueIndexer
{
	@Override
	public String getName()
	{
		return "DURATION";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.DURATION;
	}

	@Override
	public SearchFieldType<PeriodDuration> getSearchFieldType()
	{
		return SearchFieldType.forLong()
			.map(
				timestamp -> {
					var days = timestamp / 86400;
					return PeriodDuration.of(
						Period.ofDays((int) days),
						Duration.ofMillis(timestamp % 86400)
					);
				},
				object -> {
					var period = object.getPeriod();
					var duration = object.getDuration();

					var days = period.toTotalMonths() * 31 + period.getDays();
					return days * 86400 + duration.toMillis();
				}
			)
			.build();
	}
}
