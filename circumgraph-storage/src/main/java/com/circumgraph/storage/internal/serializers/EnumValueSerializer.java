package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class EnumValueSerializer
	implements ValueSerializer<SimpleValue>
{
	private final EnumDef def;

	public EnumValueSerializer(EnumDef def)
	{
		this.def = def;
	}

	@Override
	public TypeDef getType()
	{
		return def;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return SimpleValue.create(def, in.readString());
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		out.writeString((String) object.get());
	}
}
