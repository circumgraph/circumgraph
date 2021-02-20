package com.circumgraph.storage.types;

import java.io.IOException;

import com.circumgraph.model.TypeDef;
import com.circumgraph.values.Value;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;

/**
 * Serializer for storing
 */
public interface ValueSerializer<V extends Value>
	extends Serializer<V>
{
	/**
	 * Get the type this handles.
	 *
	 * @return
	 */
	TypeDef getType();

	@Override
	V read(StreamingInput in)
		throws IOException;

	@Override
	void write(V object, StreamingOutput out)
		throws IOException;
}
