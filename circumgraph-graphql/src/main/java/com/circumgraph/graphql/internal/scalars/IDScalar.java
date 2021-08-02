package com.circumgraph.graphql.internal.scalars;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import se.l4.silo.Transaction;
import se.l4.ylem.ids.LongIdCodec;

public class IDScalar
	implements ScalarMapper<Long>
{
	private final LongIdCodec<String> idCodec;

	public IDScalar(LongIdCodec<String> idCodec)
	{
		this.idCodec = idCodec;
	}

	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.ID;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return Scalars.GraphQLID;
	}

	@Override
	public Long fromInput(Object inputValue)
	{
		return idCodec.decode(inputValue.toString());
	}

	@Override
	public Object toOutput(Transaction tx, SimpleValue value)
	{
		return value == null ? null : idCodec.encode(value.asID());
	}
}
