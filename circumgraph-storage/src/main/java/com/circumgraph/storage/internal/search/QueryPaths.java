package com.circumgraph.storage.internal.search;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.SearchIndexException;
import se.l4.silo.index.search.query.FieldQuery;

public class QueryPaths
{
	public static QueryPath.Branch create(StructuredDef def)
	{
		return new BranchImpl(null, def, false);
	}

	private static class BranchImpl
		implements QueryPath.Branch
	{
		private final String path;

		private final StructuredDef def;
		private final boolean polymorphic;

		public BranchImpl(
			String path,
			StructuredDef def,
			boolean polymorphic
		)
		{
			this.path = path;
			this.def = def;
			this.polymorphic = polymorphic;
		}

		private String path(String next)
		{
			var self = (polymorphic ? def.getName() : "_") + '.' + next;
			return path == null ? self : path + '.' + self;
		}

		@Override
		public Branch polymorphic(StructuredDef type)
		{
			return new BranchImpl(path, type, true);
		}

		@Override
		public QueryPath field(String field)
		{
			var def = this.def.getField(field)
				.get()
				.getType();

			if(def instanceof StructuredDef)
			{
				return new BranchImpl(
					path(field),
					(StructuredDef) def,
					false
				);
			}
			else
			{
				return new LeafImpl(path(field));
			}
		}

		@Override
		public Branch branch(String field)
		{
			var def = this.def.getField(field)
				.get()
				.getType();

			if(! (def instanceof StructuredDef))
			{
				throw new SearchIndexException();
			}

			return new BranchImpl(
				path(field),
				(StructuredDef) def,
				false
			);
		}

		@Override
		public Leaf leaf(String field)
		{
			return new LeafImpl(path(field));
		}
	}

	private static class LeafImpl
		implements QueryPath.Leaf
	{
		private final String path;

		public LeafImpl(String path)
		{
			this.path = path;
		}

		@Override
		public FieldQuery toQuery(Matcher<?> matcher)
		{
			return FieldQuery.create(path, matcher);
		}
	}
}
