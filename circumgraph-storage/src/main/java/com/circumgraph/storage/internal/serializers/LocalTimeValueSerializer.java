package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.LocalTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link ScalarDef#LOCAL_TIME}. Stores {@link LocalTime}
 * as the nano-seconds of the day.
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
			LocalTime.ofNanoOfDay(timestamp)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var time = object.as(LocalTime.class);
		out.writeLong(time.toNanoOfDay());
	}
}
