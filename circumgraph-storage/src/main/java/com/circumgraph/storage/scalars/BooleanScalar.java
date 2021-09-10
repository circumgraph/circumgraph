package com.circumgraph.storage.scalars;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class BooleanScalar
	implements Scalar<Boolean, Boolean>
{
	public static final BooleanScalar INSTANCE = new BooleanScalar();

	@Override
	public ScalarDef getModelType()
	{
		return ScalarDef.BOOLEAN;
	}

	@Override
	public Class<Boolean> getGraphQLType()
	{
		return Boolean.class;
	}

	@Override
	public Class<Boolean> getJavaType()
	{
		return Boolean.class;
	}

	@Override
	public Boolean toGraphQL(Boolean in)
	{
		return in;
	}

	@Override
	public Boolean toJava(Object in)
	{
		if(in instanceof Boolean b)
		{
			return b;
		}
		else if(in instanceof String s)
		{
			if(s.equalsIgnoreCase("true"))
			{
				return true;
			}
			else if(s.equalsIgnoreCase("false"))
			{
				return false;
			}

			throw new ScalarConversionException("Invalid Boolean format, value was: " + in);
		}

		throw new ScalarConversionException("Invalid Boolean, value was: " + in);
	}

	@Override
	public Boolean read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.readBoolean();
	}

	@Override
	public void write(Boolean object, StreamingOutput out)
		throws IOException
	{
		out.writeBoolean(object);
	}
}
