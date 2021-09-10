package com.circumgraph.storage.types.offsetdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.circumgraph.storage.internal.serializers.ValueSerializerTest;
import com.circumgraph.storage.scalars.OffsetDateTimeScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class OffsetDateTimeScalarTest
	extends ValueSerializerTest
{
	@Test
	public void testToGraphQLWithUTC()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toGraphQL(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(converted, is("2020-01-01T08:45:30.1Z"));
	}

	@Test
	public void testToGraphQLWithOffset()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toGraphQL(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(converted, is("2020-01-01T08:45:30.1+01:00"));
	}

	@Test
	public void testToJavaOffsetUTC()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toJava("2020-01-01T08:45:30.1Z");

		assertThat(converted, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testToJavaOffset1()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toJava("2020-01-01T08:45:30.1+01:00");

		assertThat(converted, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testToJavaOffsetNamed()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toJava("2020-01-01T08:45:30.1[Europe/Stockholm]");

		assertThat(converted, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testToJavaNoZone()
	{
		var scalar = new OffsetDateTimeScalar();
		var converted = scalar.toJava("2020-01-01T08:45:30.1");

		assertThat(converted, is(
			OffsetDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeInt(0);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			OffsetDateTime.of(2021, 9, 2, 5, 58, 0, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testReadOneHourOffset()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeInt(3600);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			OffsetDateTime.of(2021, 9, 2, 6, 58, 0, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();

		var in = write(out -> {
			instance.write(
				OffsetDateTime.of(2021, 9, 2, 5, 58, 0, 100000000, ZoneOffset.UTC),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(0));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteOneHourOffset()
		throws IOException
	{
		var instance = new OffsetDateTimeScalar();

		var in = write(out -> {
			instance.write(
				OffsetDateTime.of(2021, 9, 2, 6, 58, 0, 100000000, ZoneOffset.ofHours(1)),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(3600));

		assertThat(in.next(), is(Token.LIST_END));
	}
}
