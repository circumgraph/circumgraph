package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueSerializer;

import org.eclipse.collections.api.map.MapIterable;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Handler that takes care of polymorphism. This type of value actually handles
 * {@link ObjectDef objects} or references to stored objects.
 */
public class PolymorphicValueSerializer
	implements ValueSerializer<Value>
{
	private final OutputTypeDef def;
	private final MapIterable<String, ValueSerializer<?>> subTypes;

	public PolymorphicValueSerializer(
		OutputTypeDef def,
		MapIterable<String, ValueSerializer<?>> subTypes
	)
	{
		this.def = def;
		this.subTypes = subTypes;
	}

	@Override
	public TypeDef getType()
	{
		return def;
	}

	@Override
	public Value read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		in.next(Token.VALUE);
		String typename = in.readString();

		Value result;

		ValueSerializer<?> subType = subTypes.get(typename);
		if(subType == null)
		{
			in.skipNext();

			// TODO: Can we actually return null here safely?
			result = null;
		}
		else
		{
			result = subType.read(in);
		}

		in.next(Token.LIST_END);
		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void write(Value object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(2);

		String typename = object.getDefinition().getName();
		out.writeString(typename);

		((ValueSerializer) subTypes.get(typename)).write(object, out);

		out.writeListEnd();
	}
}
