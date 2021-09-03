package com.circumgraph.graphql.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.circumgraph.graphql.scalars.ZonedDateTimeScalar;

import org.junit.jupiter.api.Test;

import graphql.language.StringValue;

public class ZonedDateTimeScalarTest
{
	@Test
	public void testSerializationWithNamedZone()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		);

		assertThat(data, is("2020-01-01T08:45:30.1+01:00[Europe/Stockholm]"));
	}

	@Test
	public void testSerializationWithUTC()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(data, is("2020-01-01T08:45:30.1Z"));
	}

	@Test
	public void testSerializationWithOffset()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(data, is("2020-01-01T08:45:30.1+01:00"));
	}

	@Test
	public void testParseValueFull()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1+01:00[Europe/Stockholm]");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testParseValueOffsetUTC()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1Z");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseValueOffset1()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1+01:00");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testParseValueNoZone()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseValueLazyZone()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1[Europe/Stockholm]");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testParseValueWrongOffsetForZone()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1+02:00[Europe/Stockholm]");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 7, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testParseLiteralFull()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseLiteral(new StringValue(
			"2020-01-01T08:45:30.1+01:00[Europe/Stockholm]"
		));

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}
}
