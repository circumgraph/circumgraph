package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class LocalDateTimeScalar
	implements Scalar<LocalDateTime, String>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.LOCAL_DATE_TIME;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<LocalDateTime> getJavaType()
	{
		return LocalDateTime.class;
	}

	@Override
	public String toGraphQL(LocalDateTime in)
	{
		return in.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	@Override
	public LocalDateTime toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid LocalDateTime format, expected to be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid LocalDateTime, value was: " + in);
	}

	@Override
	public LocalDateTime read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var timestamp = in.readLong();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
	}

	@Override
	public void write(LocalDateTime object, StreamingOutput out)
		throws IOException
	{
		out.writeLong(object.toInstant(ZoneOffset.UTC).toEpochMilli());
	}
}
