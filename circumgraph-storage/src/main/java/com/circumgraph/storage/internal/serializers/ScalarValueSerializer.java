package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.scalars.Scalar;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;

/**
 * {@link ValueSerializer} that uses a {@link Scalar} to handle the raw
 * value.
 */
public class ScalarValueSerializer
	implements ValueSerializer<SimpleValue>
{
	private final Scalar<?, ?> scalar;

	public ScalarValueSerializer(Scalar<?, ?> scalar)
	{
		this.scalar = scalar;
	}

	@Override
	public TypeDef getType()
	{
		return scalar.getModelType();
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		var value = scalar.read(in);
		return SimpleValue.create(scalar.getModelType(), value);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		((Scalar) scalar).write(object.get(), out);
	}
}
