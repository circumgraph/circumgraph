package com.circumgraph.graphql.internal;

import java.util.List;

import com.circumgraph.storage.scalars.Scalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class ScalarCoercing<I, O>
	implements Coercing<I, O>
{
	private final Scalar<I, O> scalar;

	public ScalarCoercing(Scalar<I, O> scalar)
	{
		this.scalar = scalar;
	}

	@Override
	public O serialize(Object dataFetcherResult)
		throws CoercingSerializeException
	{
		if(scalar.getJavaType().isAssignableFrom(dataFetcherResult.getClass()))
		{
			try
			{
				return scalar.toGraphQL(scalar.getJavaType().cast(dataFetcherResult));
			}
			catch(ScalarConversionException e)
			{
				throw new CoercingSerializeException(e.getMessage(), e);
			}
		}

		throw new CoercingSerializeException();
	}

	@Override
	public I parseValue(Object input)
		throws CoercingParseValueException
	{
		if(scalar.getGraphQLType().isAssignableFrom(input.getClass()))
		{
			try
			{
				return scalar.toJava(scalar.getGraphQLType().cast(input));
			}
			catch(ScalarConversionException e)
			{
				throw new CoercingParseValueException(e.getMessage(), e);
			}
		}

		throw new CoercingParseValueException();
	}

	@Override
	public I parseLiteral(Object input)
		throws CoercingParseLiteralException
	{
		if(input instanceof Value<?> v)
		{
			var javaValue = toJavaValue(v);
			if(scalar.getGraphQLType().isAssignableFrom(javaValue.getClass()))
			{
				try
				{
					return scalar.toJava(scalar.getGraphQLType().cast(javaValue));
				}
				catch(ScalarConversionException e)
				{
					throw new CoercingParseLiteralException(e.getMessage(), e);
				}
			}
		}

		throw new CoercingParseLiteralException();
	}

	/**
	 * Convert a {@link Value} into a Java object.
	 *
	 * @param input
	 * @return
	 */
	private static Object toJavaValue(Value<?> input)
	{
		if(input instanceof NullValue)
		{
			return null;
		}
		else if(input instanceof FloatValue)
		{
			return ((FloatValue) input).getValue();
		}
		else if(input instanceof StringValue)
		{
			return ((StringValue) input).getValue();
		}
		else if (input instanceof IntValue)
		{
			return ((IntValue) input).getValue().intValue();
		}
		else if (input instanceof BooleanValue)
		{
			return ((BooleanValue) input).isValue();
		}
		else if (input instanceof EnumValue)
		{
			return ((EnumValue) input).getName();
		}
		else if(input instanceof ArrayValue)
		{
			return Lists.immutable.ofAll(((ArrayValue) input).getValues())
				.collect(ScalarCoercing::toJavaValue);
		}
		else if(input instanceof ObjectValue)
		{
			List<ObjectField> values = ((ObjectValue) input).getObjectFields();
			MutableMap<String, Object> parsedValues = Maps.mutable.empty();
			values.forEach(field -> {
				Object parsedValue = toJavaValue(field.getValue());
				parsedValues.put(field.getName(), parsedValue);
			});
			return parsedValues.toImmutable();
		}

		throw new CoercingParseLiteralException();
	}
}
