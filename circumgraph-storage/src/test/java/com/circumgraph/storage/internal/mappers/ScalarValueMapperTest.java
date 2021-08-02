package com.circumgraph.storage.internal.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;

import org.eclipse.collections.impl.collector.Collectors2;
import org.junit.jupiter.api.Test;

public class ScalarValueMapperTest
{
	@Test
	public void testNoInitialValue()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			ValueValidator.empty()
		);

		var r = mapper.getInitialValue().block();
		assertThat(r, nullValue());
	}

	@Test
	public void testInitialValue()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			ValueProvider.createStatic(SimpleValue.createString("V1")),
			ValueValidator.empty()
		);

		var r = mapper.getInitialValue().block();
		assertThat(r, is(SimpleValue.createString("V1")));
	}

	@Test
	public void testApplyMutationInitial()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			ValueValidator.empty()
		);

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
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			ValueValidator.empty()
		);

		var encounter = new TestMappingEncounter();
		var value = mapper.applyMutation(
			encounter,
			ObjectLocation.root(),
			SimpleValue.createString("V1"),
			ScalarValueMutation.createString("V2")
		).block();

		encounter.assertNoErrors();
		assertThat(value, is(SimpleValue.createString("V2")));
	}

	@Test
	public void testApplyMutationValidates()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			TestValidators.failing()
		);

		var encounter = new TestMappingEncounter();
		mapper.applyMutation(
			encounter,
			ObjectLocation.root(),
			null,
			ScalarValueMutation.createString("V1")
		).block();

		assertThat(encounter.getErrors().isEmpty(), is(false));
	}

	@Test
	public void testValidateNull()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			TestValidators.failing()
		);

		var errors = mapper.validate(
			ObjectLocation.root(),
			null
		).collect(Collectors2.toImmutableList()).block();

		assertThat(errors.isEmpty(), is(false));
	}

	@Test
	public void testValidateNonNull()
	{
		var mapper = new ScalarValueMapper(
			ScalarDef.STRING,
			null,
			TestValidators.failing()
		);

		var errors = mapper.validate(
			ObjectLocation.root(),
			SimpleValue.createString("V1")
		).collect(Collectors2.toImmutableList()).block();

		assertThat(errors.isEmpty(), is(false));
	}
}
