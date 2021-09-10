package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class ZonedDateTimeScalar
	implements Scalar<ZonedDateTime, String>
{
	/**
	 * {@link DateTimeFormatter} used for more lenient parsing of timezones.
	 */
	private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
		.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		.optionalStart()
		.appendOffsetId()
		.optionalEnd()
		.optionalStart()
		.appendLiteral('[')
		.parseCaseSensitive()
		.appendZoneRegionId()
		.appendLiteral(']')
		.optionalEnd()
		.toFormatter();

	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.ZONED_DATE_TIME;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<ZonedDateTime> getJavaType()
	{
		return ZonedDateTime.class;
	}

	@Override
	public String toGraphQL(ZonedDateTime in)
	{
		return in.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
	}

	@Override
	public ZonedDateTime toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				var dt = PARSER.parseBest(
					s,
					ZonedDateTime::from,
					LocalDateTime::from
				);

				if(dt instanceof ZonedDateTime zdt)
				{
					return zdt;
				}
				else
				{
					return ((LocalDateTime) dt).atZone(ZoneOffset.UTC);
				}
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid ZonedDateTime, format is expected to be be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invaid ZonedDateTime, value was: " + in);
	}

	@Override
	public ZonedDateTime read(StreamingInput in)
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

		return ZonedDateTime.ofInstant(
			Instant.ofEpochMilli(timestamp),
			ZoneId.of(zoneId)
		);
	}

	@Override
	public void write(ZonedDateTime object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(2);
		out.writeLong(object.toInstant().toEpochMilli());
		out.writeString(object.getZone().normalized().getId());
		out.writeListEnd();
	}
}
