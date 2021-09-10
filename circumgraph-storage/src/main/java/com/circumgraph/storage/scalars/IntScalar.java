package com.circumgraph.storage.scalars;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class IntScalar
	implements Scalar<Integer, Integer>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.INT;
	}

	@Override
	public Class<Integer> getGraphQLType()
	{
		return Integer.class;
	}

	@Override
	public Class<Integer> getJavaType()
	{
		return Integer.class;
	}

	@Override
	public Integer toGraphQL(Integer in)
	{
		return in;
	}

	@Override
	public Integer toJava(Object in)
	{
		if(in instanceof Integer i)
		{
			return i;
		}
		else if(in instanceof Number n)
		{
			return n.intValue();
		}
		else if(in instanceof String s)
		{
			try
			{
				return Integer.parseInt(s);
			}
			catch(NumberFormatException e)
			{
				throw new ScalarConversionException("Invalid Int format, value was: " + s);
			}
		}

		throw new ScalarConversionException("Invalid Int, value was: " + in);
	}

	@Override
	public Integer read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.readInt();
	}

	@Override
	public void write(Integer object, StreamingOutput out)
		throws IOException
	{
		out.writeInt(object);
	}
}
