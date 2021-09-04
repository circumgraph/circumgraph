package com.circumgraph.graphql.types.offsetdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.circumgraph.graphql.scalars.OffsetDateTimeScalar;

import org.junit.jupiter.api.Test;

import graphql.language.StringValue;

public class OffsetDateTimeScalarTest
{
	@Test
	public void testSerializationWithUTC()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(data, is("2020-01-01T08:45:30.1Z"));
	}

	@Test
	public void testSerializationWithOffset()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(data, is("2020-01-01T08:45:30.1+01:00"));
	}

	@Test
	public void testParseValueOffsetUTC()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1Z");

		assertThat(parsed, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseValueOffset1()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1+01:00");

		assertThat(parsed, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testParseValueOffsetNamed()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1[Europe/Stockholm]");

		assertThat(parsed, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testParseValueNoZone()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1");

		assertThat(parsed, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseLiteralFull()
	{
		var scalar = new OffsetDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseLiteral(new StringValue(
			"2020-01-01T08:45:30.1+01:00"
		));

		assertThat(parsed, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}
}
