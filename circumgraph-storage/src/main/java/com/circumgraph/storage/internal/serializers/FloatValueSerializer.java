package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class FloatValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.FLOAT;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return SimpleValue.createFloat(in.readDouble());
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		out.writeDouble((Double) object.get());
	}
}
