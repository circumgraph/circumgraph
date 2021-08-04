package com.circumgraph.storage.types.refs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;

public class StoredObjectRefIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.build()
				)
				.addField(FieldDef.create("author")
					.withType("Author")
					.addDirective(DirectiveUse.create("index")
						.build())
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Author")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("name")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var authors = storage.get("Author");

		var author = authors.store(authors.newMutation()
			.updateField("name", ScalarValueMutation.createString("Example Author"))
			.build()
		).block();

		var authorId = author.getId();

		var books = storage.get("Book");

		var book = books.store(books.newMutation()
			.updateField("title", ScalarValueMutation.createString("A short history of nearly everything"))
			.updateField("author", StoredObjectRefMutation.create(authors.getDefinition(), authorId))
			.build()
		).block();

		var authorRef = book.getField("author", StoredObjectRef.class).get();
		assertThat(authorRef.getDefinition(), is(author.getDefinition()));
		assertThat(authorRef.getId(), is(authorId));

		var fetchedBook = books.get(book.getId()).block();

		var fetchedAuthorRef = fetchedBook.getField("author", StoredObjectRef.class).get();
		assertThat(fetchedAuthorRef.getDefinition(), is(author.getDefinition()));
		assertThat(fetchedAuthorRef.getId(), is(authorId));
	}

	@Test
	public void testQuery()
	{
		var authors = storage.get("Author");

		var author = authors.store(authors.newMutation()
			.updateField("name", ScalarValueMutation.createString("Example Author"))
			.build()
		).block();

		var authorId = author.getId();

		var books = storage.get("Book");

		books.store(books.newMutation()
			.updateField("title", ScalarValueMutation.createString("A short history of nearly everything"))
			.updateField("author", StoredObjectRefMutation.create(authors.getDefinition(), authorId))
			.build()
		).block();

		var root = QueryPath.root(books.getDefinition());
		var results = books.search(
			Query.create()
				.addClause(root.field("author").toQuery(EqualsMatcher.create(authorId)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}
