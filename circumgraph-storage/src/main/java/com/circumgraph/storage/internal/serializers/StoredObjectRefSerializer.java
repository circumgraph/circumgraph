package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.internal.StoredObjectRefImpl;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class StoredObjectRefSerializer
	implements ValueSerializer<StoredObjectRef>
{
	private final StructuredDef def;

	public StoredObjectRefSerializer(StructuredDef def)
	{
		this.def = def;
	}

	@Override
	public TypeDef getType()
	{
		return def;
	}

	@Override
	public StoredObjectRef read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var id = in.readLong();
		return new StoredObjectRefImpl(def, id);
	}

	@Override
	public void write(StoredObjectRef object, StreamingOutput out)
		throws IOException
	{
		out.writeLong(object.getDecodedId());
	}
}
