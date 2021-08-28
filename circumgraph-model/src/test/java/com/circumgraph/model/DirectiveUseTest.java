package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.internal.DirectiveUseImpl;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class DirectiveUseTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(DirectiveUseImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation"
			)
			.verify();
	}

	@SuppressWarnings("rawtypes")
	private static DirectiveUseProcessor<?> processor(Class<? extends HasDirectives> t, String name)
	{
		return new DirectiveUseProcessor()
		{
			@Override
			public String getName()
			{
				return name;
			}

			@Override
			public Class getContextType()
			{
				return t;
			}

			@Override
			public void process(
				ProcessingEncounter encounter,
				HasDirectives location,
				DirectiveUse directive
			)
			{
			}
		};
	}

	@Test
	public void testDirectiveStructuredKnown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(StructuredDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var directive = type.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveStructuredUnknown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(FieldDef.create("f1")
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveStructuredWrongContext()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(FieldDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveFieldKnown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(FieldDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var directive = type.getField("f1").get()
			.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveFieldUnknown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveFieldInvalidContext()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveArgumentKnown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(ScalarDef.STRING)
						.addDirective(DirectiveUse.create("d1")
							.withDefinedAt(Location.create("LOC1"))
							.addArgument("a1", "test")
							.build()
						)
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(ArgumentDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var directive = type.getField("f1").get()
			.getArgument("a1").get()
			.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveArgumentUnknown()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(ScalarDef.STRING)
						.addDirective(DirectiveUse.create("d1")
							.withDefinedAt(Location.create("LOC1"))
							.addArgument("a1", "test")
							.build()
						)
						.build()
					)
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveArgumentInvalidContext()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType(ScalarDef.STRING)
						.addDirective(DirectiveUse.create("d1")
							.withDefinedAt(Location.create("LOC1"))
							.addArgument("a1", "test")
							.build()
						)
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveUnionKnown()
	{
		var schema = Schema.create()
			.addType(UnionDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addType(ObjectDef.create("A")
					.addField(FieldDef.create("f1")
						.withType(ScalarDef.STRING)
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(UnionDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", UnionDef.class).get();
		var directive = type.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveUnionUnknown()
	{
		var schema = Schema.create()
			.addType(UnionDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addType(ObjectDef.create("A")
					.addField(FieldDef.create("f1")
						.withType(ScalarDef.STRING)
						.build()
					)
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveUnionWrongContext()
	{
		var schema = Schema.create()
			.addType(UnionDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addType(ObjectDef.create("A")
					.addField(FieldDef.create("f1")
						.withType(ScalarDef.STRING)
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveEnumKnown()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addValue(EnumValueDef.create("V1")
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(EnumDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", EnumDef.class).get();
		var directive = type.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveEnumUnknown()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addValue(EnumValueDef.create("V1")
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveEnumWrongContext()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addValue(EnumValueDef.create("V1")
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveEnumValueKnown()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addValue(EnumValueDef.create("V1")
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(EnumValueDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", EnumDef.class).get();
		var directive = type.getValue("V1").get().getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveEnumValueUnknown()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addValue(EnumValueDef.create("V1")
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveEnumValueWrongContext()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Test")
				.addValue(EnumValueDef.create("V1")
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveInputObjectKnown()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputObjectDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InputObjectDef.class).get();
		var directive = type.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveInputObjectUnknown()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(InputFieldDef.create("f1")
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveInputObjectWrongContext()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addDirective(DirectiveUse.create("d1")
					.withDefinedAt(Location.create("LOC1"))
					.addArgument("a1", "test")
					.build()
				)
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(FieldDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveInputFieldKnown()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(InputFieldDef.class, "d1"))
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InputObjectDef.class).get();
		var directive = type.getField("f1").get()
			.getDirective("d1");
		assertThat(directive.isPresent(), is(true));

		assertThat(directive.get().getDefinedAt(), is(Location.create("LOC1")));

		var a1 = directive.get().getArgument("a1");
		assertThat(a1.isPresent(), is(true));
		assertThat(a1.get().getValueAsString(), is(Optional.of("test")));
	}

	@Test
	public void testDirectiveInputfieldUnknown()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
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
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}

	@Test
	public void testDirectiveInputFieldWrongContext()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("d1")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("a1", "test")
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(processor(FieldDef.class, "d1"))
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("model:invalid-directive"));
		assertThat(msg.getArguments().get("directive"), is("d1"));
	}
}
