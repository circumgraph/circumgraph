package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.types.ValueSerializer;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class ListValueSerializer<V extends Value>
	implements ValueSerializer<ListValue<V>>
{
	private final ListDef type;
	private final ValueSerializer<V> itemSerializer;

	public ListValueSerializer(
		ListDef type,
		ValueSerializer<V> itemsSerializer
	)
	{
		this.type = type;
		this.itemSerializer = itemsSerializer;
	}

	@Override
	public TypeDef getType()
	{
		return type;
	}

	@Override
	public ListValue<V> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		MutableList<V> items = Lists.mutable.ofInitialCapacity(
			in.getLength().orElse(5)
		);

		while(in.peek() != Token.LIST_END)
		{
			items.add(in.readObject(itemSerializer));
		}

		in.next(Token.LIST_END);

		return ListValue.create(type, items);
	}

	@Override
	public void write(ListValue<V> object, StreamingOutput out)
		throws IOException
	{
		ListIterable<V> items = object.items();
		out.writeListStart(items.size());

		for(V v : items)
		{
			out.writeObject(itemSerializer, v);
		}

		out.writeListEnd();
	}
}
