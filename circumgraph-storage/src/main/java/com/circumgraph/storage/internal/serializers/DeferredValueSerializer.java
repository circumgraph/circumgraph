package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.ValueSerializers;
import com.circumgraph.storage.types.ValueSerializer;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;

public class DeferredValueSerializer
	implements ValueSerializer<Value>
{
	private final HasMetadata def;

	private ValueSerializer<?> serializer;

	public DeferredValueSerializer(HasMetadata def)
	{
		this.def = def;
	}

	@Override
	public TypeDef getType()
	{
		return (TypeDef) def;
	}

	@SuppressWarnings("rawtypes")
	private ValueSerializer serializer()
	{
		var serializer = this.serializer;
		if(serializer == null)
		{
			serializer = def.getMetadata(ValueSerializers.SERIALIZER).get();
			if(serializer instanceof DeferredValueSerializer)
			{
				throw new ModelException("Internal error; Can not resolve non-deferred serializer for " + def);
			}

			this.serializer = serializer;
		}

		return serializer;
	}

	@Override
	public Value read(StreamingInput in)
		throws IOException
	{
		return serializer().read(in);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void write(Value object, StreamingOutput out)
		throws IOException
	{
		serializer().write(object, out);
	}
}
