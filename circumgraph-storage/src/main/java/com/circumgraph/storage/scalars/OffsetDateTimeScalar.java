package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class OffsetDateTimeScalar
	implements Scalar<OffsetDateTime, String>
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
		return ScalarDef.OFFSET_DATE_TIME;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<OffsetDateTime> getJavaType()
	{
		return OffsetDateTime.class;
	}

	@Override
	public String toGraphQL(OffsetDateTime in)
	{
		return in.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	@Override
	public OffsetDateTime toJava(Object in)
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
					return zdt.toOffsetDateTime();
				}
				else
				{
					return ((LocalDateTime) dt).atOffset(ZoneOffset.UTC);
				}
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid OffsetDateTime format, expected to be be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid OffsetDateTime, value was: " + in);
	}

	@Override
	public OffsetDateTime read(StreamingInput in)
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

		return OffsetDateTime.ofInstant(
			Instant.ofEpochMilli(timestamp),
			ZoneOffset.ofTotalSeconds(zoneSeconds)
		);
	}

	@Override
	public void write(OffsetDateTime object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(2);
		out.writeLong(object.toInstant().toEpochMilli());
		out.writeInt(object.getOffset().getTotalSeconds());
		out.writeListEnd();
	}
}
