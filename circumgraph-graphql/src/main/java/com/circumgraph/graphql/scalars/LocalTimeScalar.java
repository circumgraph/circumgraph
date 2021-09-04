package com.circumgraph.graphql.scalars;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public class LocalTimeScalar
	implements ScalarMapper<LocalTime>
{
	private final GraphQLScalarType type;

	public LocalTimeScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("LocalTime")
			.description("Representation of a time in the ISO8601 standard")
			.coercing(new Coercing<LocalTime, String>()
			{
				@Override
				public String serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof LocalTime localTime)
					{
						return localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
					}

					throw new CoercingSerializeException("Expected type 'LocalTime' but was: " + dataFetcherResult);
				}

				@Override
				public LocalTime parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return LocalTime.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_TIME);
					}
					catch(DateTimeParseException e)
					{
						throw new CoercingParseLiteralException("Invalid LocalTime format, format is expected to be ISO8601 compatible");
					}
				}

				@Override
				public LocalTime parseLiteral(Object input)
					throws CoercingParseLiteralException
				{
					if(input instanceof StringValue stringValue)
					{
						try
						{
							return LocalTime.parse(stringValue.getValue(), DateTimeFormatter.ISO_LOCAL_TIME);
						}
						catch(DateTimeParseException e)
						{
							throw new CoercingParseLiteralException("Invalid LocalTime format, format is expected to be ISO8601 compatible");
						}
					}

					throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'");
				}
			})
			.build();
	}

	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.LOCAL_TIME;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
