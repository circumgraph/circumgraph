package com.circumgraph.graphql.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import com.circumgraph.storage.scalars.IntScalar;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class ScalarCoercingTest
{
	@Test
	public void testToJavaValueNull()
	{
		var converted = ScalarCoercing.toJavaValue(NullValue.newNullValue().build());
		assertThat(converted, nullValue());
	}

	@Test
	public void testToJavaValueString()
	{
		var converted = ScalarCoercing.toJavaValue(StringValue.newStringValue("value").build());
		assertThat(converted, is("value"));
	}

	@Test
	public void testToJavaValueInt()
	{
		var converted = ScalarCoercing.toJavaValue(IntValue.newIntValue(BigInteger.valueOf(100)).build());
		assertThat(converted, is(100));
	}

	@Test
	public void testToJavaValueBoolean()
	{
		var converted = ScalarCoercing.toJavaValue(BooleanValue.newBooleanValue(true).build());
		assertThat(converted, is(true));
	}

	@Test
	public void testToJavaValueEnumValue()
	{
		var converted = ScalarCoercing.toJavaValue(EnumValue.newEnumValue("name").build());
		assertThat(converted, is("name"));
	}

	@Test
	public void testToJavaValueArray()
	{
		var converted = ScalarCoercing.toJavaValue(ArrayValue.newArrayValue()
			.value(StringValue.newStringValue("v1").build())
			.value(StringValue.newStringValue("v2").build())
			.build());

		assertThat(converted, is(Lists.immutable.of("v1", "v2")));
	}

	@Test
	public void testToJavaValueObject()
	{
		var converted = ScalarCoercing.toJavaValue(ObjectValue.newObjectValue()
			.objectField(ObjectField.newObjectField()
				.name("k1")
				.value(StringValue.newStringValue("v1").build())
				.build()
			)
			.build());

		assertThat(converted, is(Maps.immutable.of("k1", "v1")));
	}

	@Test
	public void testSerializeSuccess()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		var converted = coercing.serialize(100);

		assertThat(converted, is(100));
	}

	@Test
	public void testSerializeFailure()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		assertThrows(CoercingSerializeException.class, () -> coercing.serialize("abc"));
	}

	@Test
	public void testParseValueSuccess()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		var converted = coercing.parseValue("100");

		assertThat(converted, is(100));
	}

	@Test
	public void testParseValueFailure()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		assertThrows(CoercingParseValueException.class, () -> coercing.parseValue("abc"));
	}

	@Test
	public void testParseLiteralSuccess()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		var converted = coercing.parseLiteral(StringValue.newStringValue()
			.value("100")
			.build()
		);

		assertThat(converted, is(100));
	}

	@Test
	public void testParseLiteralFailure()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		assertThrows(CoercingParseLiteralException.class, () -> coercing.parseLiteral(StringValue.newStringValue()
			.value("abc")
			.build()
		));
	}

	@Test
	public void testParseLiteralNonValue()
	{
		var coercing = new ScalarCoercing<>(new IntScalar());
		assertThrows(CoercingParseLiteralException.class, () -> coercing.parseLiteral("abc"));
	}
}
