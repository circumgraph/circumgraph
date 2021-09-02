package com.circumgraph.graphql.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.circumgraph.graphql.scalars.ZonedDateTimeScalar;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;

public class ZonedDateTimeScalarTest
{
	@Test
	public void testSerialization()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var data = coercing.serialize(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		);

		assertThat(data, instanceOf(Map.class));

		var map = (Map) data;
		assertThat(map.get("dateTime"), is("2020-01-01T08:45:30.1"));
		assertThat(map.get("zone"), is("Europe/Stockholm"));
	}

	@Test
	public void testParseValueObject()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var map = new HashMap<>();
		map.put("dateTime", "2020-01-01T08:45:30.1");
		map.put("zone", "Europe/Stockholm");

		var parsed = coercing.parseValue(map);

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testParseValueObjectNoZone()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var map = new HashMap<>();
		map.put("dateTime", "2020-01-01T08:45:30.1");

		var parsed = coercing.parseValue(map);

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseValueString()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var parsed = coercing.parseValue("2020-01-01T08:45:30.1Z");

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testParseLiteralFull()
	{
		var scalar = new ZonedDateTimeScalar();
		var coercing = scalar.getGraphQLType().getCoercing();

		var value = new ObjectValue(Lists.mutable.of(
			new ObjectField("dateTime", new StringValue("2020-01-01T08:45:30.1")),
			new ObjectField("zone", new StringValue("Europe/Stockholm"))
		));

		var parsed = coercing.parseLiteral(value);

		assertThat(parsed, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}
}
