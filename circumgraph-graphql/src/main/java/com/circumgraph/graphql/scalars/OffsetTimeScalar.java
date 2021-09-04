package com.circumgraph.graphql.scalars;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public class OffsetTimeScalar
	implements ScalarMapper<OffsetTime>
{
	private final GraphQLScalarType type;

	/**
	 * {@link DateTimeFormatter} used for more lenient parsing of timezones.
	 */
	private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
		.append(DateTimeFormatter.ISO_LOCAL_TIME)
		.optionalStart()
		.appendOffsetId()
		.optionalEnd()
		.optionalStart()
		.toFormatter();

	public OffsetTimeScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("OffsetTime")
			.description("Representation of a time with a time zone offset")
			.coercing(new Coercing<OffsetTime, Object>()
			{
				@Override
				public Object serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof OffsetTime OffsetTime)
					{
						return OffsetTime.format(DateTimeFormatter.ISO_OFFSET_TIME);
					}

					throw new CoercingSerializeException("Expected type 'OffsetTime' but was: " + dataFetcherResult);
				}

				@Override
				public OffsetTime parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return parse(input);
					}
					catch(DateTimeException e)
					{
						throw new CoercingParseValueException("Invalid OffsetTime format, format is expected to be ISO8601 compatible; " + e.getMessage());
					}
				}

				@Override
				public OffsetTime parseLiteral(Object input)
					throws CoercingParseLiteralException
				{
					try
					{
						if(input instanceof StringValue stringValue)
						{
							return parse(stringValue.getValue());
						}

						throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'");
					}
					catch(DateTimeException e)
					{
						throw new CoercingParseLiteralException("Invalid OffsetTime format");
					}

				}

				private OffsetTime parse(Object input)
				{
					if(! (input instanceof String string))
					{
						throw new DateTimeException("Unable to convert value: " + input);
					}

					var dt = PARSER.parseBest(
						string,
						OffsetTime::from,
						LocalTime::from
					);

					if(dt instanceof OffsetTime ot)
					{
						return ot;
					}
					else
					{
						return ((LocalTime) dt).atOffset(ZoneOffset.UTC);
					}
				}
			})
			.build();
	}

	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.OFFSET_TIME;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
