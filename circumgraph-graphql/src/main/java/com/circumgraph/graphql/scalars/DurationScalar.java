package com.circumgraph.graphql.scalars;

import java.time.format.DateTimeParseException;

import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.ScalarDef;

import org.threeten.extra.PeriodDuration;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public class DurationScalar
	implements ScalarMapper<PeriodDuration>
{
	private final GraphQLScalarType type;

	public DurationScalar()
	{
		this.type = GraphQLScalarType.newScalar()
			.name("Duration")
			.description("Representation of a duration")
			.coercing(new Coercing<PeriodDuration, String>()
			{
				@Override
				public String serialize(Object dataFetcherResult)
					throws CoercingSerializeException
				{
					if(dataFetcherResult instanceof PeriodDuration pd)
					{
						return pd.toString();
					}

					throw new CoercingSerializeException("Expected type 'PeriodDuration' but was: " + dataFetcherResult);
				}

				@Override
				public PeriodDuration parseValue(Object input)
					throws CoercingParseValueException
				{
					try
					{
						return PeriodDuration.parse(input.toString());
					}
					catch(DateTimeParseException e)
					{
						throw new CoercingParseValueException("Invalid Duration format, format is expected to be ISO8601 compatible");
					}
				}

				@Override
				public PeriodDuration parseLiteral(Object input)
					throws CoercingParseLiteralException
				{
					if(input instanceof StringValue stringValue)
					{
						try
						{
							return PeriodDuration.parse(stringValue.getValue());
						}
						catch(DateTimeParseException e)
						{
							throw new CoercingParseLiteralException("Invalid Duration format, format is expected to be ISO8601 compatible");
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
		return ScalarDef.DURATION;
	}

	@Override
	public GraphQLScalarType getGraphQLType()
	{
		return type;
	}
}
