package com.circumgraph.storage.internal.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class ReadOnlyMapperTest
{
	@Test
	public void testApplyMutationInitial()
	{
		var mapper =  new ReadOnlyMapper<>(new ScalarValueMapper(
			ScalarDef.STRING
		));

		var encounter = new TestMappingEncounter();
		var value = mapper.applyMutation(
			encounter,
			ObjectLocation.root(),
			null,
			ScalarValueMutation.createString("V1")
		).block();

		encounter.assertNoErrors();
		assertThat(value, is(SimpleValue.createString("V1")));
	}

	@Test
	public void testApplyMutationUpdate()
	{
		var mapper = new ReadOnlyMapper<>(new ScalarValueMapper(
			ScalarDef.STRING
		));

		var encounter = new TestMappingEncounter();
		mapper.applyMutation(
			encounter,
			ObjectLocation.root(),
			SimpleValue.createString("V1"),
			ScalarValueMutation.createString("V2")
		).block();

		assertThat(encounter.getErrors().isEmpty(), is(false));
	}
}
