package com.circumgraph.storage.types.booleans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.BooleanScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class BooleanScalarTest
	extends ScalarTest
{
	@Test
	public void testTypes()
		throws IOException
	{
		var instance = new BooleanScalar();
		assertThat(instance.getModelType(), is(ScalarDef.BOOLEAN));
		assertThat(instance.getGraphQLType(), is(Boolean.class));
		assertThat(instance.getJavaType(), is(Boolean.class));
	}

	@Test
	public void testToGraphQL()
		throws IOException
	{
		var instance = new BooleanScalar();
		var converted = instance.toGraphQL(false);
		assertThat(converted, is(false));
	}

	@Test
	public void testToJava()
		throws IOException
	{
		var instance = new BooleanScalar();
		var converted = instance.toJava(true);
		assertThat(converted, is(true));
	}

	@Test
	public void testToJavaStringFalse()
		throws IOException
	{
		var instance = new BooleanScalar();
		var converted = instance.toJava("false");
		assertThat(converted, is(false));
	}

	@Test
	public void testToJavaStringTrue()
		throws IOException
	{
		var instance = new BooleanScalar();
		var converted = instance.toJava("true");
		assertThat(converted, is(true));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new BooleanScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("truthy"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new BooleanScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new BooleanScalar();

		var in = write(out -> {
			out.writeBoolean(true);
		});

		var value = instance.read(in);
		assertThat(value, is(true));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new BooleanScalar();

		var in = write(out -> {
			instance.write(
				false,
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readBoolean(), is(false));
	}
}
