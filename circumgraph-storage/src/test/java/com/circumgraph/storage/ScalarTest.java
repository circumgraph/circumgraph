package com.circumgraph.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.circumgraph.storage.scalars.Scalar;

import se.l4.exobytes.streaming.StreamingFormat;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.ylem.io.IOConsumer;

/**
 * Base class for tests of {@link Scalar} implementations.
 */
public abstract class ScalarTest
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
