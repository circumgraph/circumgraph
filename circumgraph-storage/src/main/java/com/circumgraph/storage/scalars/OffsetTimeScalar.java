package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class OffsetTimeScalar
	implements Scalar<OffsetTime, String>
{
	/**
	 * {@link DateTimeFormatter} used for more lenient parsing of offsets.
	 */
	private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
		.append(DateTimeFormatter.ISO_LOCAL_TIME)
		.optionalStart()
		.appendOffsetId()
		.optionalEnd()
		.toFormatter();

	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.OFFSET_TIME;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<OffsetTime> getJavaType()
	{
		return OffsetTime.class;
	}

	@Override
	public String toGraphQL(OffsetTime in)
	{
		return in.format(DateTimeFormatter.ISO_OFFSET_TIME);
	}

	@Override
	public OffsetTime toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				var dt = PARSER.parseBest(
					s,
					OffsetTime::from,
					LocalTime::from
				);

				if(dt instanceof OffsetTime ot)
				{
					return ot;
				}
				else
				{
					return ((LocalTime) dt).atOffset(ZoneOffset.UTC);
				}
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid OffsetTime format, expected to be be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid OffsetTime, value was: " + in);
	}

	@Override
	public OffsetTime read(StreamingInput in)
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

		return LocalTime.ofNanoOfDay(timestamp)
			.atOffset(ZoneOffset.ofTotalSeconds(zoneSeconds));
	}

	@Override
	public void write(OffsetTime object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(2);
		out.writeLong(object.toLocalTime().toNanoOfDay());
		out.writeInt(object.getOffset().getTotalSeconds());
		out.writeListEnd();
	}
}
