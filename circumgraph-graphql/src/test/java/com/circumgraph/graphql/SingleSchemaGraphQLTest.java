package com.circumgraph.graphql;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.junit.jupiter.api.BeforeEach;

public abstract class SingleSchemaGraphQLTest
	extends GraphQLTest
{
	protected abstract String getSchema();

	protected Context ctx;

	@BeforeEach
	public void setup()
	{
		ctx = open(getSchema());
	}

	public Result execute(String query)
	{
		return ctx.execute(query);
	}

	public Result execute(String query, MapIterable<String, Object> variables)
	{
		return ctx.execute(query, Maps.mutable.ofMapIterable(variables));
	}
}
