package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.types.ValueSerializer;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class StructuredValueSerializer
	implements ValueSerializer<StructuredValue>
{
	private final StructuredDef type;
	private final MapIterable<String, ValueSerializer<?>> fields;

	public StructuredValueSerializer(
		StructuredDef type,
		MapIterable<String, ValueSerializer<?>> fields
	)
	{
		this.type = type;
		this.fields = fields;
	}

	@Override
	public TypeDef getType()
	{
		return type;
	}

	@Override
	public StructuredValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.OBJECT_START);

		MutableMap<String, Value> values = Maps.mutable.ofInitialCapacity(
			in.getLength().orElse(5)
		);

		while(in.peek() != Token.OBJECT_END)
		{
			in.next();
			String field = in.readString();

			ValueSerializer<?> serializer = fields.get(field);
			if(serializer == null)
			{
				// No serializer, the field has been removed so the value has to be skipped
				in.skipNext();
			}
			else
			{
				values.put(field, serializer.read(in));
			}
		}

		in.next(Token.OBJECT_END);

		return StructuredValue.create(type, values);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void write(StructuredValue object, StreamingOutput out)
		throws IOException
	{
		MapIterable<String, ? extends Value> fields = object.getFields();
		out.writeObjectStart(fields.size());

		for(Pair<String, ? extends Value> e : fields.keyValuesView())
		{
			ValueSerializer serializer = this.fields.get(e.getOne());
			if(serializer == null)
			{
				throw new IOException("Tried writing field that does not exist: " + e.getOne());
			}

			out.writeString(e.getOne());
			serializer.write(e.getTwo(), out);
		}

		out.writeObjectEnd();
	}
}
