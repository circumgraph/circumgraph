package com.circumgraph.storage.scalars;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class FloatScalar
	implements Scalar<Double, Double>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.FLOAT;
	}

	@Override
	public Class<Double> getGraphQLType()
	{
		return Double.class;
	}

	@Override
	public Class<Double> getJavaType()
	{
		return Double.class;
	}

	@Override
	public Double toGraphQL(Double in)
	{
		return in;
	}

	@Override
	public Double toJava(Object in)
	{
		if(in instanceof Double d)
		{
			return d;
		}
		else if(in instanceof Number n)
		{
			return n.doubleValue();
		}
		else if(in instanceof String s)
		{
			try
			{
				return Double.parseDouble(s);
			}
			catch(NumberFormatException e)
			{
				throw new ScalarConversionException("Invalid Float, value was: " + s);
			}
		}

		throw new ScalarConversionException("Invalid Float, value was: " + in);
	}

	@Override
	public Double read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.readDouble();
	}

	@Override
	public void write(Double object, StreamingOutput out)
		throws IOException
	{
		out.writeDouble(object);
	}
}
