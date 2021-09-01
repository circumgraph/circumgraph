package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link ScalarDef#LOCAL_DATE}.
 */
public class LocalDateValueSerializer
	implements ValueSerializer<SimpleValue>
{
	public LocalDateValueSerializer()
	{
	}

	@Override
	public TypeDef getType()
	{
		return ScalarDef.LOCAL_DATE;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var timestamp = in.readLong();
		return SimpleValue.create(
			ScalarDef.LOCAL_DATE,
			LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var date = object.as(LocalDate.class);
		out.writeLong(date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC) * 1000);
	}
}
