package com.circumgraph.storage.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.circumgraph.storage.internal.serializers.PolymorphicValueSerializer;
import com.circumgraph.values.StructuredValue;

import se.l4.exobytes.streaming.StreamingFormat;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.silo.engine.EntityCodec;

/**
 * Codec used for values.
 */
public class EntityCodecImpl
	implements EntityCodec<StructuredValue>
{
	private final PolymorphicValueSerializer serializer;

	public EntityCodecImpl(
		PolymorphicValueSerializer serializer
	)
	{
		this.serializer = serializer;
	}

	@Override
	public StructuredValue decode(InputStream in0)
		throws IOException
	{
		try(StreamingInput in = StreamingFormat.CBOR.createInput(in0))
		{
			return serializer.read(in);
		}
	}

	@Override
	public void encode(StructuredValue instance, OutputStream out0)
		throws IOException
	{
		try(StreamingOutput out = StreamingFormat.CBOR.createOutput(out0))
		{
			serializer.write(instance, out);
		}
	}
}
