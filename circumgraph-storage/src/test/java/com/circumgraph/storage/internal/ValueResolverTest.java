package com.circumgraph.storage.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.StructuredValue;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValueResolver}.
 */
public class ValueResolverTest
{
	@Test
	public void testResolveStringScalar()
	{
		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			ScalarDef.STRING,
			"value"
		);

		assertThat(resolved, is(SimpleValue.create(ScalarDef.STRING, "value")));
	}

	@Test
	public void testResolveStringScalarNonNull()
	{
		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			NonNullDef.input(ScalarDef.STRING),
			"value"
		);

		assertThat(resolved, is(SimpleValue.create(ScalarDef.STRING, "value")));
	}

	@Test
	public void testResolveIntScalarWithConversion()
	{
		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			ScalarDef.INT,
			"100"
		);

		assertThat(resolved, is(SimpleValue.create(ScalarDef.INT, 100)));
	}

	@Test
	public void testResolveIntScalarWithFailedConversion()
	{
		var e = assertThrows(StorageValidationException.class, () -> ValueResolver.resolve(
			ObjectLocation.root(),
			ScalarDef.INT,
			"abc"
		));

		var issue = e.getIssues().getFirst();
		assertThat(issue.getCode(), is("value:invalid-scalar-value"));
	}

	@Test
	public void testResolveEnum()
	{
		var enumDef = EnumDef.create("Test")
			.addValue(EnumValueDef.create("A")
				.build()
			)
			.build();

		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			enumDef,
			"A"
		);

		assertThat(resolved, is(SimpleValue.create(enumDef, "A")));
	}

	@Test
	public void testResolveEnumInvalid()
	{
		var enumDef = EnumDef.create("Test")
			.addValue(EnumValueDef.create("A")
				.build()
			)
			.build();

		var e = assertThrows(StorageValidationException.class, () -> ValueResolver.resolve(
			ObjectLocation.root(),
			enumDef,
			"B"
		));

		var issue = e.getIssues().getFirst();
		assertThat(issue.getCode(), is("value:invalid-enum-value"));
	}

	@Test
	public void testResolveList()
	{
		var listDef = ListDef.output(ScalarDef.STRING);

		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			listDef,
			Lists.immutable.of("A", "B")
		);

		assertThat(resolved, is(ListValue.create(
			listDef,
			Lists.immutable.of(
				SimpleValue.createString("A"),
				SimpleValue.createString("B")
			)
		)));
	}

	@Test
	public void testResolveListInvalid()
	{
		var listDef = ListDef.output(ScalarDef.STRING);

		var e = assertThrows(StorageValidationException.class, () -> ValueResolver.resolve(
			ObjectLocation.root(),
			listDef,
			"B"
		));

		var issue = e.getIssues().getFirst();
		assertThat(issue.getCode(), is("value:invalid-list-value"));
	}

	@Test
	public void testResolveObject()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var objectDef = model.get("Test", ObjectDef.class).get();

		var resolved = ValueResolver.resolve(
			ObjectLocation.root(),
			objectDef,
			Maps.immutable.of(
				"f1", "A"
			)
		);

		assertThat(resolved, is(StructuredValue.create(objectDef)
			.add("f1", SimpleValue.createString("A"))
			.build()
		));
	}

	@Test
	public void testResolveObjectInvalid()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var objectDef = model.get("Test", ObjectDef.class).get();

		var e = assertThrows(StorageValidationException.class, () -> ValueResolver.resolve(
			ObjectLocation.root(),
			objectDef,
			"B"
		));

		var issue = e.getIssues().getFirst();
		assertThat(issue.getCode(), is("value:invalid-object-value"));
	}

	@Test
	public void testResolveObjectInvalidField()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var objectDef = model.get("Test", ObjectDef.class).get();

		var e = assertThrows(StorageValidationException.class, () -> ValueResolver.resolve(
			ObjectLocation.root(),
			objectDef,
			Maps.immutable.of(
				"f2", "A"
			)
		));

		var issue = e.getIssues().getFirst();
		assertThat(issue.getCode(), is("value:invalid-field"));
	}
}
