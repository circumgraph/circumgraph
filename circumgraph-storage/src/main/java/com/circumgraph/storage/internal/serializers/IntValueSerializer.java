package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class IntValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.INT;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return SimpleValue.createInt(in.readInt());
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		out.writeInt((Integer) object.get());
	}
}
