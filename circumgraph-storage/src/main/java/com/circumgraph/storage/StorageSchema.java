package com.circumgraph.storage;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.storage.internal.ValueIndexers;
import com.circumgraph.storage.internal.ValueProviders;
import com.circumgraph.storage.internal.model.DefaultDirectiveValidator;
import com.circumgraph.storage.internal.model.IndexDirectiveValidator;
import com.circumgraph.storage.internal.model.ReadonlyDirectiveValidator;
import com.circumgraph.storage.internal.model.SortableDirectiveValidator;

import org.eclipse.collections.api.factory.Lists;

/**
 * Schema describing the built-in types that the storage system requires.
 */
// TODO: Validation that types only implement Entity *once* either directly or via a single interface
public class StorageSchema
	implements Schema
{
	public static final String ENTITY_NAME = "Entity";

	public static final StorageSchema INSTANCE = new StorageSchema();

	private StorageSchema()
	{
	}

	@Override
	public Iterable<? extends DirectiveValidator<?>> getDirectiveValidators()
	{
		return Lists.immutable.of(
			new DefaultDirectiveValidator(new ValueProviders()),
			new ReadonlyDirectiveValidator(),

			new IndexDirectiveValidator(new ValueIndexers()),
			new SortableDirectiveValidator()
		);
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return Lists.immutable.of(
			InterfaceDef.create(ENTITY_NAME)
				.addField(FieldDef.create("id")
					.withType(NonNullDef.output(ScalarDef.ID))
					.addDirective(DirectiveUse.create("default")
						.addArgument("provider", "ID")
						.build()
					)
					.addDirective(DirectiveUse.create("readonly")
						.build()
					)
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
		);
	}
}
