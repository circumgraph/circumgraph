package com.circumgraph.graphql.scalars;

import java.time.DateTimeException;
import java.time.LocalDateTime;
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

/**
 * Scalar for {@link ZonedDateTime}.
 *
 * The representation of this in the GraphQL API is an object with two fields:
 *
 * <ul>
 *   <li>{@code dateTime} - the ISO8601 formatted date, without offset
 *   <li>{@code zone} - the timezone ID
 * </ul>
 *
 *
 */
public class ZonedDateTimeScalar
	implements ScalarMapper<ZonedDateTime>
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

	public ZonedDateTimeScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("ZonedDateTime")
			.description("Representation of a date, time and time zone")
			.coercing(new Coercing<ZonedDateTime, Object>()
			{
				@Override
				public Object serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof ZonedDateTime zonedDateTime)
					{
						return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
					}

					throw new CoercingSerializeException("Expected type 'ZonedDateTime' but was: " + dataFetcherResult);
				}

				@Override
				public ZonedDateTime parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return parse(input);
					}
					catch(DateTimeException e)
					{
						throw new CoercingParseValueException("Invalid ZonedDateTime format, format is expected to be ISO8601 compatible; " + e.getMessage());
					}
				}

				@Override
				public ZonedDateTime parseLiteral(Object input)
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
						throw new CoercingParseLiteralException("Invalid ZonedDateTime format");
					}

				}

				private ZonedDateTime parse(Object input)
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
						return zdt;
					}
					else
					{
						return ((LocalDateTime) dt).atZone(ZoneOffset.UTC);
					}
				}
			})
			.build();
	}

	@Override
	public ScalarDef getModelDef()
	{
		return ScalarDef.ZONED_DATE_TIME;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
