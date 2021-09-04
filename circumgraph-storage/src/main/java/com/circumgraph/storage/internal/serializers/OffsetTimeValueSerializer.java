package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class OffsetTimeValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.OFFSET_TIME;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		// Read the timestamp
		in.next(Token.VALUE);
		var timestamp = in.readLong();

		// Read the zone offset
		in.next(Token.VALUE);
		var zoneSeconds = in.readInt();

		in.next(Token.LIST_END);

		return SimpleValue.create(
			ScalarDef.OFFSET_TIME,
			LocalTime.ofNanoOfDay(timestamp)
				.atOffset(ZoneOffset.ofTotalSeconds(zoneSeconds))
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var date = object.as(OffsetTime.class);
		out.writeListStart(2);
		out.writeLong(date.toLocalTime().toNanoOfDay());
		out.writeInt(date.getOffset().getTotalSeconds());
		out.writeListEnd();
	}
}
