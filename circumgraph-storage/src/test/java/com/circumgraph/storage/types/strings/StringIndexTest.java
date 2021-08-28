package com.circumgraph.storage.types.strings;

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

/**
 * Test that simply tests that indexers can be created for {@code String}
 * types.
 */
public class StringIndexTest
	extends StorageTest
{
	@Test
	public void testDefault()
	{
		open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);
	}

	@Test
	public void testFullText()
	{
		open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "FULL_TEXT")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);
	}

	@Test
	public void tesTypeAhead()
	{
		open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "TYPE_AHEAD")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);
	}

	@Test
	public void testToken()
	{
		open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "TOKEN")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);
	}

	@Test
	public void testUnknown()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("type", "THIS_DOES_NOT_EXIST")
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
		assertThat(msg.getCode(), is("storage:@index:invalid-indexer"));
		assertThat(msg.getArguments().get("indexer"), is("THIS_DOES_NOT_EXIST"));
		assertThat(msg.getMessage(), is("The indexer `THIS_DOES_NOT_EXIST` does not exist"));
	}

	@Test
	public void testUnsupportedType()
	{
		var e = assertThrows(ModelValidationException.class, () -> open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("field")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.withDefinedAt(Location.create("LOC1"))
						.addArgument("type", "INT")
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
		assertThat(msg.getCode(), is("storage:@index:indexer-unsupported-type"));
		assertThat(msg.getArguments().get("indexer"), is("INT"));
		assertThat(msg.getArguments().get("fieldType"), is("String"));
		assertThat(msg.getMessage(), is("The indexer `INT` does not support `String`"));
	}
}
