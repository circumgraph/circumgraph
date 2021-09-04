package com.circumgraph.graphql.scalars;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

public class OffsetDateTimeScalar
	implements ScalarMapper<OffsetDateTime>
{
	private final GraphQLScalarType type;

	/**
	 * {@link DateTimeFormatter} used for more lenient parsing of timezones.
	 */
	private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
		.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		.optionalStart()
		.appendOffsetId()
		.optionalEnd()
		.optionalStart()
		.appendLiteral('[')
		.parseCaseSensitive()
		.appendZoneRegionId()
		.appendLiteral(']')
		.optionalEnd()
		.toFormatter();

	public OffsetDateTimeScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("OffsetDateTime")
			.description("Representation of a date, time and time zone offset")
			.coercing(new Coercing<OffsetDateTime, Object>()
			{
				@Override
				public Object serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof OffsetDateTime offsetDateTime)
					{
						return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
					}

					throw new CoercingSerializeException("Expected type 'OffsetDateTime' but was: " + dataFetcherResult);
				}

				@Override
				public OffsetDateTime parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return parse(input);
					}
					catch(DateTimeException e)
					{
						throw new CoercingParseValueException("Invalid OffsetDateTime format, format is expected to be ISO8601 compatible; " + e.getMessage());
					}
				}

				@Override
				public OffsetDateTime parseLiteral(Object input)
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
						throw new CoercingParseLiteralException("Invalid OffsetDateTime format");
					}

				}

				private OffsetDateTime parse(Object input)
				{
					if(! (input instanceof String string))
					{
						throw new DateTimeException("Unable to convert value: " + input);
					}

					var dt = PARSER.parseBest(
						string,
						ZonedDateTime::from,
						LocalDateTime::from
					);

					if(dt instanceof ZonedDateTime zdt)
					{
						return zdt.toOffsetDateTime();
					}
					else
					{
						return ((LocalDateTime) dt).atOffset(ZoneOffset.UTC);
					}
				}
			})
			.build();
	}

	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.OFFSET_DATE_TIME;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
