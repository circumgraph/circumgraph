package com.circumgraph.graphql;

import com.circumgraph.graphql.internal.processors.MutationProcessor;
import com.circumgraph.graphql.internal.processors.OutputProcessor;
import com.circumgraph.graphql.internal.processors.QueryProcessor;
import com.circumgraph.graphql.internal.processors.RelationDirectiveProcessor;
import com.circumgraph.graphql.internal.search.SearchQueryGenerator;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
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
			new OutputProcessor(),
			new QueryProcessor(new SearchQueryGenerator()),
			new MutationProcessor()
		);
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return Lists.immutable.of();
	}
}
