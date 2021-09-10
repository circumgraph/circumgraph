package com.circumgraph.storage.scalars;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class StringScalar
	implements Scalar<String, String>
{
	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.STRING;
	}

	@Override
	public Class<String> getGraphQLType()
	{
		return String.class;
	}

	@Override
	public Class<String> getJavaType()
	{
		return String.class;
	}

	@Override
	public String toGraphQL(String in)
	{
		return in;
	}

	@Override
	public String toJava(Object in)
	{
		if(in == null)
		{
			throw new ScalarConversionException("Invalid String, value was: " + in);
		}

		return in.toString();
	}

	@Override
	public String read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.readString();
	}

	@Override
	public void write(String object, StreamingOutput out)
		throws IOException
	{
		out.writeString(object);
	}
}
