package com.circumgraph.storage.internal.serializers;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.types.ValueSerializer;

import org.threeten.extra.PeriodDuration;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

public class DurationValueSerializer
	implements ValueSerializer<SimpleValue>
{
	@Override
	public TypeDef getType()
	{
		return ScalarDef.DURATION;
	}

	@Override
	public SimpleValue read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return SimpleValue.create(
			ScalarDef.DURATION,
			PeriodDuration.parse(in.readString())
		);
	}

	@Override
	public void write(SimpleValue object, StreamingOutput out)
		throws IOException
	{
		var periodDuration = object.as(PeriodDuration.class);
		out.writeString(periodDuration.toString());
	}
}
