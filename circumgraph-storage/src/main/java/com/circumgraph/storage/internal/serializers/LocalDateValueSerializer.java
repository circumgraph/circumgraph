package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.LocalDate;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link ScalarDef#LOCAL_DATE}. Stores {@link LocalDate} as
 * epoch days.
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
		var days = in.readLong();
		return SimpleValue.create(
			ScalarDef.LOCAL_DATE,
			LocalDate.ofEpochDay(days)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var date = object.as(LocalDate.class);
		out.writeLong(date.toEpochDay());
	}
}
