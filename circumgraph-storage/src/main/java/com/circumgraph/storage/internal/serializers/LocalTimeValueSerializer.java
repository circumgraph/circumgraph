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
 * Serializer for {@link ScalarDef#LOCAL_TIME}.
 */
public class LocalTimeValueSerializer
	implements ValueSerializer<SimpleValue>
{
	public TypeDef getType()
	{
		return ScalarDef.LOCAL_TIME;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var timestamp = in.readLong();
		return SimpleValue.create(
			ScalarDef.LOCAL_TIME,
			LocalTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var time = object.as(LocalTime.class);
		var instantAtEpoch = Instant.ofEpochSecond(
			time.toEpochSecond(LocalDate.EPOCH, ZoneOffset.UTC),
			time.getNano()
		);
		out.writeLong(instantAtEpoch.toEpochMilli());
	}
}
