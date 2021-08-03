package com.circumgraph.storage.internal.search;

import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.StorageException;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.query.FieldQuery;

/**
 * Implementation of {@link QueryPath}.
 */
public class QueryPathImpl
	implements QueryPath
{
	private final QueryPath parent;

	private final OutputTypeDef def;
	private final String self;

	public QueryPathImpl(
		QueryPath parent,
		OutputTypeDef def,
		String self
	)
	{
		this.parent = parent;
		this.self = self;
		this.def = def;
	}

	@Override
	public QueryPath polymorphic(StructuredDef type)
	{
		return this.def == type ? this : new QueryPathImpl(parent, type, type.getName());
	}

	@Override
	public QueryPath field(String fieldName)
	{
		if(! (this.def instanceof StructuredDef))
		{
			throw new StorageException(this.toIndexName() + ": At a field that is not a StructuredDef");
		}

		var structuredDef = (StructuredDef) this.def;
		var field = structuredDef.getField(fieldName);
		if(field.isEmpty())
		{
			throw new StorageException(this.toIndexName() + ": The field " + fieldName + " does not exist");
		}

		var def = field.get().getType();
		if(def instanceof NonNullDef.Output)
		{
			def = ((NonNullDef.Output) def).getType();
		}

		var fieldPath = new QueryPathImpl(
			this.polymorphic(field.get().getDeclaringType()),
			def,
			fieldName
		);

		if(def instanceof StructuredDef && ! ((StructuredDef) def).findImplements(StorageSchema.ENTITY_NAME))
		{
			return new QueryPathImpl(
				fieldPath,
				def,
				def.getName()
			);
		}

		return fieldPath;
	}

	@Override
	public QueryPath typename()
	{
		return new QueryPathImpl(
			this,
			null,
			"__typename"
		);
	}

	@Override
	public boolean isRoot()
	{
		return parent == null;
	}

	@Override
	public String toIndexName()
	{
		return (parent == null ? "" : parent.toIndexName() + ".") + self;
	}

	@Override
	public FieldQuery toQuery(Matcher<?> matcher)
	{
		return FieldQuery.create(toIndexName(), matcher);
	}

	public static QueryPath create(StructuredDef def)
	{
		return new QueryPathImpl(null, def, def.getName());
	}
}
