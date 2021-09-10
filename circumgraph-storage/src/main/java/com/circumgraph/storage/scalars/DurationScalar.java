package com.circumgraph.storage.scalars;

import java.io.IOException;
import java.time.DateTimeException;

import com.circumgraph.model.ScalarDef;

import org.threeten.extra.PeriodDuration;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class DurationScalar
	implements Scalar<PeriodDuration, String>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.DURATION;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<PeriodDuration> getJavaType()
	{
		return PeriodDuration.class;
	}

	@Override
	public String toGraphQL(PeriodDuration in)
	{
		return in.toString();
	}

	@Override
	public PeriodDuration toJava(Object in)
	{
		if(in instanceof String s)
		{
			try
			{
				return PeriodDuration.parse(s);
			}
			catch(DateTimeException e)
			{
				throw new ScalarConversionException("Invalid Duration format, expected to be ISO8601 compatible");
			}
		}

		throw new ScalarConversionException("Invalid Duration, value was: " + in);
	}

	@Override
	public PeriodDuration read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return PeriodDuration.parse(in.readString());
	}

	@Override
	public void write(PeriodDuration object, StreamingOutput out)
		throws IOException
	{
		out.writeString(object.toString());
	}
}
