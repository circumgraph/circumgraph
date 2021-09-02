package com.circumgraph.storage.internal.serializers;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link ScalarDef#ZONED_DATE_TIME}. Stores {@link java.time.ZonedDateTime}
 * with a millisecond precision and zone information.
 */
public class ZonedDateTimeValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.ZONED_DATE_TIME;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		// Read the timestamp
		in.next(Token.VALUE);
		var timestamp = in.readLong();

		// Read the zone id
		in.next(Token.VALUE);
		var zoneId = in.readString();

		in.next(Token.LIST_END);

		return SimpleValue.create(
			ScalarDef.ZONED_DATE_TIME,
			ZonedDateTime.ofInstant(
				Instant.ofEpochMilli(timestamp),
				ZoneId.of(zoneId)
			)
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var date = object.as(ZonedDateTime.class);
		out.writeListStart(2);
		out.writeLong(date.toInstant().toEpochMilli());
		out.writeString(date.getZone().normalized().getId());
		out.writeListEnd();
	}
}
