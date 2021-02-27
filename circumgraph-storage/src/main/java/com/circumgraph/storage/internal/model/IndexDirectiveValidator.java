package com.circumgraph.storage.internal.model;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.storage.internal.EntityIndexing;

/**
 * Validator for the {@code index} directive.
 */
public class IndexDirectiveValidator
	implements DirectiveValidator<FieldDef>
{
	private final EntityIndexing indexing;

	public IndexDirectiveValidator(
		EntityIndexing indexing
	)
	{
		this.indexing = indexing;
	}

	@Override
	public String getName()
	{
		return "index";
	}

	@Override
	public Class<FieldDef> getContextType()
	{
		return FieldDef.class;
	}

	@Override
	public void validate(
		FieldDef location,
		DirectiveUse directive,
		ValidationMessageCollector collector
	)
	{
		SimpleValueDef def;
		if(location.getType() instanceof StructuredDef
			&& ((StructuredDef) location.getType()).findImplements("Entity"))
		{
			// Link to another entity
			def = ScalarDef.ID;
		}
		else if(location.getType() instanceof SimpleValueDef)
		{
			// Simple edge value
			def = (SimpleValueDef) location.getType();
		}
		else
		{
			error(location, directive, collector);
			return;
		}

		var type = directive.getArgument("type");
		if(type.isEmpty())
		{
			// No type argument, resolve via best
			var indexer = indexing.guessBestIndexer(def);
			if(indexer.isPresent()) return;

			// No indexer, let's check if none available or many available
			if(indexing.hasMultipleIndexers(def))
			{
				collector.error()
					.withLocation(directive.getSourceLocation())
					.withMessage("The type %s has multiple indexers, need to specify type on @index", def.getName())
					.withCode("storage:index-type-multiple")
					.withArgument("type", def.getName())
					.done();
			}
			else
			{
				error(location, directive, collector);
			}
		}
		else
		{
			var typeName = (String) type.get().getValue();
			var indexer = indexing.getIndexer(typeName);
			if(indexer.isEmpty() || indexer.get().getType() != def)
			{
				collector.error()
					.withLocation(directive.getSourceLocation())
					.withMessage("The type %s does not support @index with %s", def.getName(), type)
					.withCode("storage:index-type-unsupported")
					.withArgument("type", location.getType().getName())
					.withArgument("indexType", type)
					.done();
			}
		}
	}

	private void error(
		FieldDef location,
		DirectiveUse directive,
		ValidationMessageCollector collector
	)
	{
		collector.error()
			.withLocation(directive.getSourceLocation())
			.withMessage("The type %s does not support @index", location.getType().getName())
			.withCode("storage:index-type-unsupported")
			.withArgument("type", location.getType().getName())
			.done();
	}
}
