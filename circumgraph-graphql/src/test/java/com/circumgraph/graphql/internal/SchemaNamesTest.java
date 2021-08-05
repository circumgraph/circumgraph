package com.circumgraph.graphql.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SchemaNames}.
 */
public class SchemaNamesTest
{
	@Test
	public void testToLowerCamel()
	{
		assertThat(
			SchemaNames.toLowerCamel("Single"),
			is("single")
		);

		assertThat(
			SchemaNames.toLowerCamel("TwoWords"),
			is("twoWords")
		);

		assertThat(
			SchemaNames.toLowerCamel("HTTP"),
			is("http")
		);

		assertThat(
			SchemaNames.toLowerCamel("HTTPProvider"),
			is("httpProvider")
		);

		assertThat(
			SchemaNames.toLowerCamel("lower"),
			is("lower")
		);

		assertThat(
			SchemaNames.toLowerCamel("lowerStart"),
			is("lowerStart")
		);

		assertThat(
			SchemaNames.toLowerCamel("1234"),
			is("1234")
		);

		assertThat(
			SchemaNames.toLowerCamel("1234test"),
			is("1234test")
		);

		assertThat(
			SchemaNames.toLowerCamel("1234Test"),
			is("1234Test")
		);

		assertThat(
			SchemaNames.toLowerCamel("Test12"),
			is("test12")
		);
	}

	@Test
	public void testToUpperSnakeCase()
	{
		assertThat(
			SchemaNames.toUpperSnakeCase("Single"),
			is("SINGLE")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("TwoWords"),
			is("TWO_WORDS")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("HTTP"),
			is("HTTP")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("HTTPProvider"),
			is("HTTP_PROVIDER")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("ProviderHTTP"),
			is("PROVIDER_HTTP")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("ProviderHTTPThing"),
			is("PROVIDER_HTTP_THING")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("lower"),
			is("LOWER")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("lowerStart"),
			is("LOWER_START")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("1234"),
			is("1234")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("1234test"),
			is("1234_TEST")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("1234Test"),
			is("1234_TEST")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("Test12"),
			is("TEST_12")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("test12"),
			is("TEST_12")
		);

		assertThat(
			SchemaNames.toUpperSnakeCase("TEST12"),
			is("TEST_12")
		);
	}
}
