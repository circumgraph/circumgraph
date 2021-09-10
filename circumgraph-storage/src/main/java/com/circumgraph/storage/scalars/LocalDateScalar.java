package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Implementation of {@link ScalarDef#LOCAL_DATE}. The Java representation of
 * this scalar uses {@link LocalDate}, with a string representation based on
 * the ISO8601 format.
 *
 * Serialization stores values as epoch-days in the form of a {@code long}.
 */
public class LocalDateScalar
	implements Scalar<LocalDate, String>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.LOCAL_DATE;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<LocalDate> getJavaType()
	{
		return LocalDate.class;
	}

	@Override
	public String toGraphQL(LocalDate in)
	{
		return in.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	@Override
	public LocalDate toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid LocalDate format, expected to be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid LocalDate, value was: " + in);
	}

	@Override
	public LocalDate read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var days = in.readLong();
		return LocalDate.ofEpochDay(days);
	}

	@Override
	public void write(LocalDate object, StreamingOutput out)
		throws IOException
	{
		out.writeLong(object.toEpochDay());
	}
}
