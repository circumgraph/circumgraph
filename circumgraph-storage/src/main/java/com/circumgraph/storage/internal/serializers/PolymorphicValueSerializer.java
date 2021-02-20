package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.types.ValueSerializer;
import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.map.MapIterable;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Handler that takes care of polymorphism. This type of value actually handles
 * {@link ObjectDef objects} and requires that these are mapped to a set of
 * {@link ValueSerializer}s.
 */
public class PolymorphicValueSerializer
	implements ValueSerializer<StructuredValue>
{
	private final StructuredDef definition;
	private final MapIterable<String, ValueSerializer<StructuredValue>> subTypes;

	public PolymorphicValueSerializer(
		StructuredDef definition,
		MapIterable<String, ValueSerializer<StructuredValue>> subTypes
	)
	{
		this.definition = definition;
		this.subTypes = subTypes;
	}

	@Override
	public TypeDef getType()
	{
		return definition;
	}

	@Override
	public StructuredValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		in.next(Token.VALUE);
		String typename = in.readString();

		StructuredValue result;

		ValueSerializer<StructuredValue> subType = subTypes.get(typename);
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
	public void write(StructuredValue object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(2);

		String typename = object.getDefinition().getName();
		out.writeString(typename);

		subTypes.get(typename).write(object, out);

		out.writeListEnd();
	}
}
