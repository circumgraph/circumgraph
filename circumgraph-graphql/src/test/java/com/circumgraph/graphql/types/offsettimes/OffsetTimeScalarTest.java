package com.circumgraph.graphql.types.offsettimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.circumgraph.graphql.scalars.OffsetTimeScalar;

import org.junit.jupiter.api.Test;

import graphql.language.StringValue;

public class OffsetTimeScalarTest
{
	@Test
	public void testSerializationWithUTC()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(data, is("08:45:30.1Z"));
	}

	@Test
	public void testSerializationWithOffset()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(data, is("08:45:30.1+01:00"));
	}

	@Test
	public void testParseValueOffsetUTC()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("08:45:30.1Z");

		assertThat(parsed, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseValueOffset1()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("08:45:30.1+01:00");

		assertThat(parsed, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testParseValueNoZone()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("08:45:30.1");

		assertThat(parsed, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseLiteralFull()
	{
		var scalar = new OffsetTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseLiteral(new StringValue(
			"08:45:30.1+01:00"
		));

		assertThat(parsed, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}
}
