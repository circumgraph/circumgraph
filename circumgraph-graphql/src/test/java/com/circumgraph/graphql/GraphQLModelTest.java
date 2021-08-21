package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ScalarDef;

import org.junit.jupiter.api.Test;

public class GraphQLModelTest
{
	@Test
	public void testSetFieldResolver()
	{
		var field = FieldDef.create("f")
			.withType(ScalarDef.STRING)
			.build();


		FieldResolver resolver = e -> {
			return null;
		};

		GraphQLModel.setFieldResolver(field, resolver);

		assertThat(field.getMetadata(GraphQLModel.FIELD_RESOLVER), is(Optional.of(resolver)));
	}

	@Test
	public void testSetFieldResolverFactory()
	{
		var field = FieldDef.create("f")
			.withType(ScalarDef.STRING)
			.build();


		FieldResolverFactory factory = e -> {
			return null;
		};

		GraphQLModel.setFieldResolverFactory(field, factory);

		assertThat(field.getMetadata(GraphQLModel.FIELD_RESOLVER_FACTORY), is(Optional.of(factory)));
	}
}
