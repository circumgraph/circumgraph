package com.circumgraph.storage.types.offsettimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.OffsetTimeScalar;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class OffsetTimeScalarTest
	extends ScalarTest
{
	@Test
	public void testTypes()
	{
		var instance = new OffsetTimeScalar();
		assertThat(instance.getModelType(), is(ScalarDef.OFFSET_TIME));
		assertThat(instance.getGraphQLType(), is(String.class));
		assertThat(instance.getJavaType(), is(OffsetTime.class));
	}

	@Test
	public void testToGraphQLWithUTC()
	{
		var scalar = new OffsetTimeScalar();
		var converted = scalar.toGraphQL(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		);

		assertThat(converted, is("08:45:30.1Z"));
	}

	@Test
	public void testToGraphQLWithOffset()
	{
		var scalar = new OffsetTimeScalar();
		var converted = scalar.toGraphQL(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		);

		assertThat(converted, is("08:45:30.1+01:00"));
	}

	@Test
	public void testToJavaOffsetUTC()
	{
		var scalar = new OffsetTimeScalar();
		var converted = scalar.toJava("08:45:30.1Z");

		assertThat(converted, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testToJavaOffset1()
	{
		var scalar = new OffsetTimeScalar();
		var converted = scalar.toJava("08:45:30.1+01:00");

		assertThat(converted, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testToJavaNoZone()
	{
		var scalar = new OffsetTimeScalar();
		var converted = scalar.toJava("08:45:30.1");

		assertThat(converted, is(
			OffsetTime.of(8, 45, 30, 100000000, ZoneOffset.UTC)
		));
	}

	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new OffsetTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(999000000l);
			out.writeInt(0);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			LocalTime.ofNanoOfDay(999000000).atOffset(ZoneOffset.UTC)
		));
	}

	@Test
	public void testReadOneHourOffset()
		throws IOException
	{
		var instance = new OffsetTimeScalar();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(999000000l);
			out.writeInt(3600);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			LocalTime.ofNanoOfDay(999000000).atOffset(ZoneOffset.ofHours(1))
		));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new OffsetTimeScalar();

		var in = write(out -> {
			instance.write(
				OffsetTime.of(1, 0, 0, 0, ZoneOffset.UTC),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(3600000000000L));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(0));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteOneHourOffset()
		throws IOException
	{
		var instance = new OffsetTimeScalar();

		var in = write(out -> {
			instance.write(
				OffsetTime.of(1, 0, 0, 0, ZoneOffset.ofHours(1)),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(3600000000000L));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(3600));

		assertThat(in.next(), is(Token.LIST_END));
	}
}
