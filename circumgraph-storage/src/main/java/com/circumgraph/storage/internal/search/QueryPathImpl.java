package com.circumgraph.storage.internal.search;

import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
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

		if(def instanceof StructuredDef )
		{
			/*
			 * For StructuredDef either descend into the object or link to it
			 * if entity.
			 */
			if(((StructuredDef) def).findImplements(StorageSchema.ENTITY_NAME))
			{
				return new QueryPathImpl(
					this.polymorphic(field.get().getDeclaringType()),
					ScalarDef.ID,
					fieldName
				);
			}
			else
			{
				return new QueryPathImpl(
					new QueryPathImpl(
						this.polymorphic(field.get().getDeclaringType()),
						def,
						fieldName
					),
					def,
					def.getName()
				);
			}
		}
		else
		{
			return new QueryPathImpl(
				this.polymorphic(field.get().getDeclaringType()),
				def,
				fieldName
			);
		}
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
		if(def instanceof StructuredDef)
		{
			// Querying the object - delegate to the __typename
			return FieldQuery.create(toIndexName() + ".__typename", matcher);
		}

		return FieldQuery.create(toIndexName(), matcher);
	}

	public static QueryPath create(StructuredDef def)
	{
		return new QueryPathImpl(null, def, def.getName());
	}
}
