package com.circumgraph.storage.internal.mappers;

import java.util.stream.Collectors;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.Value;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;

public class TestMappingEncounter
	implements MappingEncounter
{
	private final MutableList<ValidationMessage> errors;

	private final MutableSet<Value> externalValuesAdded;
	private final MutableSet<Value> externalValuesRemoved;

	private final MutableSet<Link> linksAdded;
	private final MutableSet<Link> linksRemoved;

	public TestMappingEncounter()
	{
		errors = Lists.mutable.empty();

		externalValuesAdded = Sets.mutable.empty();
		externalValuesRemoved = Sets.mutable.empty();

		linksAdded = Sets.mutable.empty();
		linksRemoved = Sets.mutable.empty();
	}

	@Override
	public void reportError(ValidationMessage message)
	{
		errors.add(message);
	}

	@Override
	public Value externalize(Value value)
	{
		externalValuesAdded.add(value);
		return value;
	}

	@Override
	public void removeExternal(Value value)
	{
		externalValuesRemoved.add(value);
	}

	@Override
	public void link(String collection, long object)
	{
		linksAdded.add(new Link(collection, object));
	}

	@Override
	public void unlink(String collection, long object)
	{
		linksRemoved.add(new Link(collection, object));
	}

	public void assertNoErrors()
	{
		if(errors.isEmpty()) return;

		AssertionError error = new AssertionError("Expected no errors, got:\n" + errors.stream()
			.map(e -> "  * " + e.getLocation().describe() + ": " + e.getMessage())
			.collect(Collectors.joining("\n"))
		);

		throw error;
	}

	public ListIterable<ValidationMessage> getErrors()
	{
		return errors;
	}

	public SetIterable<Value> getExternalValuesAdded()
	{
		return externalValuesAdded;
	}

	public SetIterable<Value> getExternalValuesRemoved()
	{
		return externalValuesRemoved;
	}

	public SetIterable<Link> getLinksAdded()
	{
		return linksAdded;
	}

	public SetIterable<Link> getLinksRemoved()
	{
		return linksRemoved;
	}

	public static class Link
	{
		private final String collection;
		private final long id;

		public Link(String collection, long id)
		{
			this.collection = collection;
			this.id = id;
		}

		public String getCollection()
		{
			return collection;
		}

		public long getId()
		{
			return id;
		}
	}
}
