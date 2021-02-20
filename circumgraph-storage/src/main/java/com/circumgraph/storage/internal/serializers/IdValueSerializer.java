package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.types.ValueSerializer;
import com.circumgraph.values.SimpleValue;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class IdValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.ID;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return SimpleValue.create(ScalarDef.ID, in.readLong());
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		out.writeLong((Long) object.get());
	}
}
