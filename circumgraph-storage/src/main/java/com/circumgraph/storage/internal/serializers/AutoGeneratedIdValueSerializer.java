package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.AutoGeneratedIdValue;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.SerializationException;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * {@link ValueSerializer} for the main {@link ScalarDef#ID} that is generated
 * by the system when objects are created.
 */
public class AutoGeneratedIdValueSerializer
	implements ValueSerializer<SimpleValue>
{
	public static final ValueSerializer<SimpleValue> INSTANCE = new AutoGeneratedIdValueSerializer();

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
		return new AutoGeneratedIdValue(in.readLong());
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		if(! (object instanceof AutoGeneratedIdValue))
		{
			throw new SerializationException("Trying to serialize a non-autogenerated id");
		}

		out.writeLong(object.asID());
	}
}
