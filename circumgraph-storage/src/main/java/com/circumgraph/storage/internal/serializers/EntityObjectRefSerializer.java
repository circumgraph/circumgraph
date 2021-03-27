package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.EntityObjectRef;
import com.circumgraph.storage.internal.EntityObjectRefImpl;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class EntityObjectRefSerializer
	implements ValueSerializer<EntityObjectRef>
{
	private final StructuredDef def;

	public EntityObjectRefSerializer(StructuredDef def)
	{
		this.def = def;
	}

	@Override
	public TypeDef getType()
	{
		return def;
	}

	@Override
	public EntityObjectRef read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		var id = in.readLong();
		return new EntityObjectRefImpl(def, id);
	}

	@Override
	public void write(EntityObjectRef object, StreamingOutput out)
		throws IOException
	{
		out.writeLong(object.getId());
	}
}
