package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Implementation of {@link ScalarDef#LOCAL_TIME}. The Java representation of
 * this scalar uses {@link LocalTime}, with a string representation based on
 * the ISO8601 format.
 *
 * Serialization supports full nano-seconds and stores values as {@code long}.
 */
public class LocalTimeScalar
	implements Scalar<LocalTime, String>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.LOCAL_TIME;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<LocalTime> getJavaType()
	{
		return LocalTime.class;
	}

	@Override
	public String toGraphQL(LocalTime in)
	{
		return in.format(DateTimeFormatter.ISO_LOCAL_TIME);
	}

	@Override
	public LocalTime toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				return LocalTime.parse(s, DateTimeFormatter.ISO_LOCAL_TIME);
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid LocalTime format, expected to be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid LocalTime, value was: " + in);
	}

	@Override
	public LocalTime read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var timestamp = in.readLong();
		return LocalTime.ofNanoOfDay(timestamp);
	}

	@Override
	public void write(LocalTime object, StreamingOutput out)
		throws IOException
	{
		out.writeLong(object.toNanoOfDay());
	}
}
