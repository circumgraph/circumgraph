package com.circumgraph.storage.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.StructuredValue;
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
	/**
	 * Tag used to indicate {@link StreamingFormat#CBOR CBOR} is used for
	 * serialization.
	 */
	private static final int TAG_CBOR = 1;

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
		int tag = in0.read();
		if(tag != TAG_CBOR)
		{
			throw new IOException("Unsupported data, only CBOR tagged data can be read. Tagged with " + tag);
		}

		try(StreamingInput in = StreamingFormat.CBOR.createInput(in0))
		{
			var object = serializer.read(in);
			return new StoredObjectValueImpl((StructuredValue) object);
		}
	}

	@Override
	public void encode(StoredObjectValue instance, OutputStream out0)
		throws IOException
	{
		out0.write(TAG_CBOR);

		try(StreamingOutput out = StreamingFormat.CBOR.createOutput(out0))
		{
			serializer.write(instance, out);
		}
	}
}
