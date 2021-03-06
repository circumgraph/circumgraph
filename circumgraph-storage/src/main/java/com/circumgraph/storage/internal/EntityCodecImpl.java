package com.circumgraph.storage.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.internal.serializers.PolymorphicValueSerializer;

import se.l4.exobytes.streaming.StreamingFormat;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.silo.engine.EntityCodec;

/**
 * Codec used for values.
 */
public class EntityCodecImpl
	implements EntityCodec<StoredEntityValue>
{
	private final PolymorphicValueSerializer serializer;

	public EntityCodecImpl(
		PolymorphicValueSerializer serializer
	)
	{
		this.serializer = serializer;
	}

	@Override
	public StoredEntityValue decode(InputStream in0)
		throws IOException
	{
		try(StreamingInput in = StreamingFormat.CBOR.createInput(in0))
		{
			var object = serializer.read(in);
			return new StoredEntityValueImpl(object);
		}
	}

	@Override
	public void encode(StoredEntityValue instance, OutputStream out0)
		throws IOException
	{
		try(StreamingOutput out = StreamingFormat.CBOR.createOutput(out0))
		{
			serializer.write(instance, out);
		}
	}
}
