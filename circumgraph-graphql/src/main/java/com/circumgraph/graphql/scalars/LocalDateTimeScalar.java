package com.circumgraph.graphql.scalars;

import java.time.LocalDateTime;
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

public class LocalDateTimeScalar
	implements ScalarMapper<LocalDateTime>
{
	private final GraphQLScalarType type;

	public LocalDateTimeScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("LocalDateTime")
			.description("Representation of a date and time in the ISO8601 standard")
			.coercing(new Coercing<LocalDateTime, String>()
			{
				@Override
				public String serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof LocalDateTime localDateTime)
					{
						return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
					}

					throw new CoercingSerializeException("Expected type 'LocalDateTime' but was: " + dataFetcherResult);
				}

				@Override
				public LocalDateTime parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return LocalDateTime.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
					}
					catch(DateTimeParseException e)
					{
						throw new CoercingParseLiteralException("Invalid LocalDateTime format, format is expected to be ISO8601 compatible");
					}
				}

				@Override
				public LocalDateTime parseLiteral(Object input)
					throws CoercingParseLiteralException
				{
					if(input instanceof StringValue stringValue)
					{
						try
						{
							return LocalDateTime.parse(stringValue.getValue(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
						}
						catch(DateTimeParseException e)
						{
							throw new CoercingParseLiteralException("Invalid LocalDateTime format, format is expected to be ISO8601 compatible");
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
		return ScalarDef.LOCAL_DATE_TIME;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
