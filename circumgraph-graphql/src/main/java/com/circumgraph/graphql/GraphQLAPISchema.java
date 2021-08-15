package com.circumgraph.graphql;

import com.circumgraph.graphql.internal.DynamicFieldProcessor;
import com.circumgraph.graphql.internal.directives.RelationDirectiveProcessor;
import com.circumgraph.model.Schema;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.TypeDefProcessor;

import org.eclipse.collections.api.factory.Lists;

/**
 * Schema for the GraphQL API.
 */
public class GraphQLAPISchema
	implements Schema
{
	@Override
	public Iterable<? extends DirectiveUseProcessor<?>> getDirectiveUseProcessors()
	{
		return Lists.immutable.of(
			new RelationDirectiveProcessor()
		);
	}

	@Override
	public Iterable<? extends TypeDefProcessor<?>> getTypeDefProcessors()
	{
		return Lists.immutable.of(
			new DynamicFieldProcessor()
		);
	}
}
