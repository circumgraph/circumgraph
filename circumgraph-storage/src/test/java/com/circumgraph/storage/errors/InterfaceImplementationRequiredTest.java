package com.circumgraph.storage.errors;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.ModelValidationException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.ModelTest;
import com.circumgraph.storage.StorageSchema;

import org.junit.jupiter.api.Test;

/**
 * Tests that checks interfaces without implementations emit a validation
 * error.
 */
public class InterfaceImplementationRequiredTest
	extends ModelTest
{
	@Test
	public void testReachableErrors1()
	{
		assertThrows(ModelValidationException.class, () -> {
			createModel(Schema.create()
				.addType(InterfaceDef.create("I")
					.build()
				)
				.addType(ObjectDef.create("T")
					.addImplements(StorageSchema.ENTITY_NAME)
					.addField(FieldDef.create("f1")
						.withType("I")
						.build()
					)
					.build()
				)
				.build()
			);
		});
	}

	@Test
	public void testReachableErrors2()
	{
		assertThrows(ModelValidationException.class, () -> {
			createModel(Schema.create()
				.addType(InterfaceDef.create("I")
					.build()
				)
				.addType(ObjectDef.create("T")
					.addImplements(StorageSchema.ENTITY_NAME)
					.addField(FieldDef.create("f1")
						.withType(NonNullDef.output("I"))
						.build()
					)
					.build()
				)
				.build()
			);
		});
	}

	@Test
	public void testReachableErrors3()
	{
		assertThrows(ModelValidationException.class, () -> {
			createModel(Schema.create()
				.addType(InterfaceDef.create("I")
					.build()
				)
				.addType(ObjectDef.create("T")
					.addImplements(StorageSchema.ENTITY_NAME)
					.addField(FieldDef.create("f1")
						.withType(ListDef.output("I"))
						.build()
					)
					.build()
				)
				.build()
			);
		});
	}

	@Test
	public void testReachableErrors4()
	{
		assertThrows(ModelValidationException.class, () -> {
			createModel(Schema.create()
				.addType(InterfaceDef.create("I")
					.build()
				)
				.addType(ObjectDef.create("T")
					.addImplements(StorageSchema.ENTITY_NAME)
					.addField(FieldDef.create("f1")
						.withType(ListDef.output(NonNullDef.output("I")))
						.build()
					)
					.build()
				)
				.build()
			);
		});
	}

	@Test
	public void testReachableErrors5()
	{
		assertThrows(ModelValidationException.class, () -> {
			createModel(Schema.create()
				.addType(InterfaceDef.create("I1")
					.build()
				)
				.addType(InterfaceDef.create("I2")
					.addImplements("I1")
					.build()
				)
				.addType(ObjectDef.create("T")
					.addImplements(StorageSchema.ENTITY_NAME)
					.addField(FieldDef.create("f1")
						.withType("I1")
						.build()
					)
					.build()
				)
				.build()
			);
		});
	}

	@Test
	public void testReachableValid1()
	{
		createModel(Schema.create()
			.addType(InterfaceDef.create("I1")
				.build()
			)
			.addType(ObjectDef.create("T2")
				.addImplements("I1")
				.build()
			)
			.addType(ObjectDef.create("T")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("f1")
					.withType("I1")
					.build()
				)
				.build()
			)
			.build()
		);
	}

	@Test
	public void testUnreachableValid1()
	{
		createModel(Schema.create()
			.addType(InterfaceDef.create("I1")
				.build()
			)
			.addType(ObjectDef.create("T1")
				.build()
			)
			.addType(ObjectDef.create("T")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("f1")
					.withType("T1")
					.build()
				)
				.build()
			)
			.build()
		);
	}
}
