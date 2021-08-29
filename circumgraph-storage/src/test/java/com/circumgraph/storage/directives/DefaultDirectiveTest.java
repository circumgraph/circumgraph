package com.circumgraph.storage.directives;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.ModelValidationException;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.storage.StorageTest;

import org.junit.jupiter.api.Test;

public class DefaultDirectiveTest
	extends StorageTest
{
	@Test
	public void testInvalidArgumentsEmpty()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("default")
						.withDefinedAt(Location.create("LOC1"))
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		));

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("storage:@default:invalid-arguments"));
		assertThat(msg.getArguments().isEmpty(), is(true));
		assertThat(msg.getMessage(), is("@default requires either provider or value to be provided"));
	}

	@Test
	public void testInvalidArgumentsUnknown()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("default")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("arg1", "")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		));

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("storage:@default:invalid-arguments"));
		assertThat(msg.getArguments().isEmpty(), is(true));
		assertThat(msg.getMessage(), is("@default requires either provider or value to be provided"));
	}

	@Test
	public void testUnknownProvider()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("default")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("provider", "THIS_DOES_NOT_EXIST")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		));

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("storage:@default:unknown-provider"));
		assertThat(msg.getArguments().get("provider"), is("THIS_DOES_NOT_EXIST"));
		assertThat(msg.getMessage(), is("The default-value provider `THIS_DOES_NOT_EXIST` is not available"));
	}

	@Test
	public void testInvalidProvider()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("default")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("provider", "ID")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		));

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getLocation(), is(Location.create("LOC1")));
		assertThat(msg.getCode(), is("storage:@default:invalid-provider"));
		assertThat(msg.getArguments().get("provider"), is("ID"));
		assertThat(msg.getArguments().get("fieldType"), is("String"));
		assertThat(msg.getMessage(), is("Default-value provider `ID` does not support the type `String`"));
	}
}
