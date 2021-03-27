package com.circumgraph.storage.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.serializers.PolymorphicValueSerializer;

import se.l4.exobytes.streaming.StreamingFormat;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.silo.engine.ObjectCodec;

/**
 * Codec built on top of {@link PolymorphicValueSerializer}.
 */
public class ObjectCodecImpl
	implements ObjectCodec<StoredObjectValue>
{
	private final PolymorphicValueSerializer serializer;

	public ObjectCodecImpl(
		PolymorphicValueSerializer serializer
	)
	{
		this.serializer = serializer;
	}

	@Override
	public StoredObjectValue decode(InputStream in0)
		throws IOException
	{
		try(StreamingInput in = StreamingFormat.CBOR.createInput(in0))
		{
			var object = serializer.read(in);
			return new StoredObjectValueImpl(object);
		}
	}

	@Override
	public void encode(StoredObjectValue instance, OutputStream out0)
		throws IOException
	{
		try(StreamingOutput out = StreamingFormat.CBOR.createOutput(out0))
		{
			serializer.write(instance, out);
		}
	}
}
