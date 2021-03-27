package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.values.ListValue;

import org.junit.jupiter.api.Test;

public class StoredObjectRefTest
	extends StorageTest
{
	@Test
	public void testStoreSimple()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("author")
					.withType("Author")
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Author")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("name")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build());

		var authors = storage.get("Author");

		var author = authors.store(authors.newMutation()
			.updateField("name", SimpleValueMutation.create("Example Author"))
			.build()
		).block();

		var authorId = author.getId();

		var books = storage.get("Book");

		var book = books.store(books.newMutation()
			.updateField("title", "A short history of nearly everything")
			.updateField("author", authorId)
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
	public void testStoreNonExistent()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("author")
					.withType("Author")
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Author")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("name")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build());

		var books = storage.get("Book");

		assertThrows(StorageException.class, () -> {
			books.store(books.newMutation()
				.updateField("title", "A short history of nearly everything")
				.updateField("author", 1l)
				.build()
			).block();
		});
	}

	@Test
	public void testStoreList()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("authors")
					.withType(ListDef.output("Author"))
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Author")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("name")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build());

		var authors = storage.get("Author");

		var author = authors.store(authors.newMutation()
			.updateField("name", SimpleValueMutation.create("Example Author"))
			.build()
		).block();

		var authorId = author.getId();

		var books = storage.get("Book");

		var book = books.store(books.newMutation()
			.updateField("title", "A short history of nearly everything")
			.updateField("authors", ListSetMutation.create(
				SimpleValueMutation.create(authorId)
			))
			.build()
		).block();

		var authorsRef = (ListValue<StoredObjectRef>) book.getField("authors").get();
		var authorRef = authorsRef.items().getFirst();
		assertThat(authorRef.getDefinition(), is(author.getDefinition()));
		assertThat(authorRef.getId(), is(authorId));

		var fetchedBook = books.get(book.getId()).block();

		var fetchedAuthorsRef = (ListValue<StoredObjectRef>) fetchedBook.getField("authors").get();
		var fetchedAuthorRef = fetchedAuthorsRef.items().getFirst();
		assertThat(fetchedAuthorRef.getDefinition(), is(author.getDefinition()));
		assertThat(fetchedAuthorRef.getId(), is(authorId));
	}

	@Test
	public void testStorePolymorphic()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Review")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("reviewBody")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("itemReviewed")
					.withType("Thing")
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("Thing")
				.addImplements(StorageSchema.ENTITY_NAME)
				.build()
			)
			.addType(ObjectDef.create("Book")
				.addImplements("Thing")
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build());

		var things = storage.get("Thing");

		var book = things.store(things.newMutation((ObjectDef) storage.getModel().get("Book").get())
			.updateField("title", SimpleValueMutation.create("A short history of nearly everything"))
			.build()
		).block();

		var bookId = book.getId();

		var reviews = storage.get("Review");

		var review = reviews.store(reviews.newMutation()
			.updateField("reviewBody", "...")
			.updateField("itemReviewed", bookId)
			.build()
		).block();

		var thingRef = review.getField("itemReviewed", StoredObjectRef.class).get();
		assertThat(thingRef.getDefinition(), is(things.getDefinition()));
		assertThat(thingRef.getId(), is(bookId));

		var fetchedReview = reviews.get(review.getId()).block();

		var fetchedThingRef = fetchedReview.getField("itemReviewed", StoredObjectRef.class).get();
		assertThat(fetchedThingRef.getDefinition(), is(things.getDefinition()));
		assertThat(fetchedThingRef.getId(), is(bookId));
	}

	@Test
	public void testStorePolymorphicSpecific()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Review")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("reviewBody")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("itemReviewed")
					.withType("Book")
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("Thing")
				.addImplements(StorageSchema.ENTITY_NAME)
				.build()
			)
			.addType(ObjectDef.create("Book")
				.addImplements("Thing")
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build());

		var things = storage.get("Thing");

		var book = things.store(things.newMutation((ObjectDef) storage.getModel().get("Book").get())
			.updateField("title", SimpleValueMutation.create("A short history of nearly everything"))
			.build()
		).block();

		var bookId = book.getId();

		var reviews = storage.get("Review");

		var review = reviews.store(reviews.newMutation()
			.updateField("reviewBody", "...")
			.updateField("itemReviewed", bookId)
			.build()
		).block();

		var thingRef = review.getField("itemReviewed", StoredObjectRef.class).get();
		assertThat(thingRef.getDefinition(), is(things.getDefinition()));
		assertThat(thingRef.getId(), is(bookId));

		var fetchedReview = reviews.get(review.getId()).block();

		var fetchedThingRef = fetchedReview.getField("itemReviewed", StoredObjectRef.class).get();
		assertThat(fetchedThingRef.getDefinition(), is(things.getDefinition()));
		assertThat(fetchedThingRef.getId(), is(bookId));
	}
}
