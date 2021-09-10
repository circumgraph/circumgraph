package com.circumgraph.storage.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.ScalarConversionException;
import com.circumgraph.storage.scalars.ZonedDateTimeScalar;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class ZonedDateTimeScalarTest
	extends ScalarTest
{
	@Test
	public void testToGraphQLWithNamedZone()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toGraphQL(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		);

		assertThat(converted, is("2020-01-01T08:45:30.1+01:00[Europe/Stockholm]"));
	}

	@Test
	public void testToGraphQLWithUTC()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toGraphQL(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(converted, is("2020-01-01T08:45:30.1Z"));
	}

	@Test
	public void testToGraphQLWithOffset()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toGraphQL(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(converted, is("2020-01-01T08:45:30.1+01:00"));
	}

	@Test
	public void testToJavaFull()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1+01:00[Europe/Stockholm]");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testToJavaOffsetUTC()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1Z");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testToJavaOffset1()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1+01:00");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testToJavaNoZone()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testToJavaLazyZone()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1[Europe/Stockholm]");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 8, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testToJavaWrongOffsetForZone()
	{
		var instance = new ZonedDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1+02:00[Europe/Stockholm]");

		assertThat(converted, is(
			ZonedDateTime.of(2020, 1, 1, 7, 45, 30, 100000000, ZoneId.of("Europe/Stockholm"))
		));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeString("Z");
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000).atZone(ZoneOffset.UTC)
		));
	}

	@Test
	public void testReadEuropeStockholm()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeString("Europe/Stockholm");
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			LocalDateTime.of(2021, 9, 2, 7, 58, 0, 100000000).atZone(ZoneId.of("Europe/Stockholm")
		)));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000).atZone(ZoneOffset.UTC),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Z"));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteEuropeStockholm()
		throws IOException
	{
		var instance = new ZonedDateTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalDateTime.of(2021, 9, 2, 7, 58, 0, 100000000).atZone(ZoneId.of("Europe/Stockholm")),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Europe/Stockholm"));

		assertThat(in.next(), is(Token.LIST_END));
	}
}
