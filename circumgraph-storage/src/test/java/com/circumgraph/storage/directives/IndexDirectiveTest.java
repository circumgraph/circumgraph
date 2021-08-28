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

public class IndexDirectiveTest
	extends StorageTest
{
	@Test
	public void testInvalidArguments()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
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
		assertThat(msg.getCode(), is("storage:@index:invalid-arguments"));
		assertThat(msg.getArguments().isEmpty(), is(true));
		assertThat(msg.getMessage(), is("@index only supports an optional type argument"));
	}

	@Test
	public void testInvalidPolymorphic()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ObjectDef.create("Sub")
						.addField(FieldDef.create("f1")
							.withType(ScalarDef.STRING)
							.build()
						)
						.build()
					)
					.addDirective(DirectiveUse.create("index")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("type", "TOKEN")
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
		assertThat(msg.getCode(), is("storage:@index:invalid-arguments-polymorphic"));
		assertThat(msg.getArguments().isEmpty(), is(true));
		assertThat(msg.getMessage(), is("Polymorphic types, such as objects, interfaces and unions can not specify an indexer type"));
	}
}
