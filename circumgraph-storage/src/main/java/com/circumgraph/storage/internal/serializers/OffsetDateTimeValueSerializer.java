package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class OffsetDateTimeValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.OFFSET_DATE_TIME;
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
			ScalarDef.OFFSET_DATE_TIME,
			OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(timestamp),
				ZoneOffset.ofTotalSeconds(zoneSeconds)
			)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var date = object.as(OffsetDateTime.class);
		out.writeListStart(2);
		out.writeLong(date.toInstant().toEpochMilli());
		out.writeInt(date.getOffset().getTotalSeconds());
		out.writeListEnd();
	}
}
