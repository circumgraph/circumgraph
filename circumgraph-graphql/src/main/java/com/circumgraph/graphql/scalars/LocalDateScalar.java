package com.circumgraph.graphql.scalars;

import java.time.LocalDate;
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

public class LocalDateScalar
	implements ScalarMapper<LocalDate>
{
	private final GraphQLScalarType type;

	public LocalDateScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("LocalDate")
			.description("Representation of a date in the ISO8601 standard")
			.coercing(new Coercing<LocalDate, String>()
			{
				@Override
				public String serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof LocalDate localDate)
					{
						return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
					}

					throw new CoercingSerializeException("Expected type 'LocalDate' but was: " + dataFetcherResult);
				}

				@Override
				public LocalDate parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return LocalDate.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
					}
					catch(DateTimeParseException e)
					{
						throw new CoercingParseValueException("Invalid LocalDate format, format is expected to be ISO8601 compatible");
					}
				}

				@Override
				public LocalDate parseLiteral(Object input)
					throws CoercingParseLiteralException
				{
					if(input instanceof StringValue stringValue)
					{
						try
						{
							return LocalDate.parse(stringValue.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
						}
						catch(DateTimeParseException e)
						{
							throw new CoercingParseLiteralException("Invalid LocalDate format, format is expected to be ISO8601 compatible");
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
		return ScalarDef.LOCAL_DATE;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}

	@Override
	public LocalDate fromInput(Object inputValue)
	{
		if(inputValue instanceof LocalDate localDate)
		{
			return localDate;
		}

		return LocalDate.parse(inputValue.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
	}
}
