package com.circumgraph.storage.internal.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import se.l4.exobytes.streaming.StreamingFormat;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.ylem.io.IOConsumer;

public class ValueSerializerTest
{
	protected StreamingInput write(IOConsumer<StreamingOutput> output)
		throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try(StreamingOutput out = StreamingFormat.CBOR.createOutput(stream))
		{
			output.accept(out);
		}

		byte[] input = stream.toByteArray();
		return StreamingFormat.CBOR.createInput(new ByteArrayInputStream(input));
	}
}
