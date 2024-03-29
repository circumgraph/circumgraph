package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.internal.FieldDefImpl;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class FieldDefTest
{
	private static MetadataKey<String> KEY = MetadataKey.create("key", String.class);

	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(FieldDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"declaringType"
			)
			.verify();
	}

	@Test
	public void testWithMetadata()
	{
		var field = FieldDef.create("test")
			.withType(ScalarDef.STRING)
			.withMetadata(KEY, "value1")
			.build();

		assertThat(field.getMetadata(KEY), is(Optional.of("value1")));
	}

	@Test
	public void testInvalidNameNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create(null)
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:invalid-name"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), nullValue());
		assertThat(msg.getMessage(), is("The name of field `null` in `Test` is not valid, should match [a-zA-Z_][a-zA-Z0-9_]*"));
	}

	@Test
	public void testInvalidNameLeadingDigit()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("1abc")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:invalid-name"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("1abc"));
		assertThat(msg.getMessage(), is("The name of field `1abc` in `Test` is not valid, should match [a-zA-Z_][a-zA-Z0-9_]*"));
	}

	@Test
	public void testDerive()
	{
		var f1 = FieldDef.create("test")
			.withType(ScalarDef.STRING)
			.withMetadata(KEY, "value1")
			.build();

		var f2 = FieldDef.create("test")
			.withType(ScalarDef.STRING)
			.withDescription("updated description")
			.withMetadata(KEY, "value1")
			.build();

		var d = f1.derive()
			.withDescription("updated description")
			.build();

		assertThat(d, is(f2));
	}

	@Test
	public void testFieldUnknownType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType("Unknown")
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldUnknownTypeInNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(NonNullDef.output("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldUnknownTypeInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldUnknownTypeInListNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(NonNullDef.output("Unknown")))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldInvalidType()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("I")
				.addField(InputFieldDef.create("if1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType("I")
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:output-type-required"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("I"));
	}

	@Test
	public void testFieldArgumentUnknownType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType("Unknown")
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("Unknown"));
	}

	@Test
	public void testFieldArgumentUnknownTypeNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(NonNullDef.input("Unknown"))
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("Unknown"));
	}

	@Test
	public void testFieldArgumentUnknownTypeInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(ListDef.input("Unknown"))
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("Unknown"));
	}

	@Test
	public void testFieldArgumentUnknownTypeInListNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(ListDef.input(NonNullDef.input("Unknown")))
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("Unknown"));
	}

	@Test
	public void testFieldArgumentInvalidType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("I")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType("I")
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:input-type-required"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("I"));
	}

	@Test
	public void testMergeField()
	{
		var schema = Schema.create()
			.addDirectiveUseProcessor(new TestDirectiveOnFieldProcessor())
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("test")
						.build())
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var def = StructuredDef.class.cast(t);
		assertThat(def.getFields(), contains(
			FieldDef.create("f1")
				.withType(ScalarDef.STRING)
				.addDirective(DirectiveUse.create("test")
					.build())
				.build()
		));
	}

	@Test
	public void testMergeFieldDifferentTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withDefinedAt(Location.create("LOC"))
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withDefinedAt(Location.create("LOC2"))
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:incompatible-field-type"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getMessage(), startsWith("Could not merge: Field `f1` in `Test` has a different type than previously defined at LOC"));
	}


	@Test
	public void testImplementsFieldSameTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldCompatibleTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType("A")
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType("B")
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldSameTypesInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldCompatibleTypesInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("A"))
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("B"))
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldDifferentTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}

	@Test
	public void testReferencedTypeHoisted()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addField(FieldDef.create("f1")
					.withType(InterfaceDef.create("B")
						.addField(FieldDef.create("f2")
							.withType(ScalarDef.INT)
							.build()
						)
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var b = model.get("B", StructuredDef.class);
		assertThat(b.isPresent(), is(true));
	}

	@Test
	public void testReferencedArgumentTypeHoisted()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(InputObjectDef.create("I")
							.addField(InputFieldDef.create("if1")
								.withType(ScalarDef.STRING)
								.build()
							)
							.build()
						)
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var b = model.get("I", InputObjectDef.class);
		assertThat(b.isPresent(), is(true));
	}

	static class TestDirectiveOnFieldProcessor
		implements DirectiveUseProcessor<FieldDef>
	{
		@Override
		public Location getLocation()
		{
			return Location.create("@test");
		}

		@Override
		public String getName()
		{
			return "test";
		}

		@Override
		public Class<FieldDef> getContextType()
		{
			return FieldDef.class;
		}

		@Override
		public void process(
			ProcessingEncounter encounter,
			FieldDef location,
			DirectiveUse directive
		)
		{
			// Always valid
		}
	}
}
