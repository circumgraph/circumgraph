package com.circumgraph.graphql.internal.search;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.resolvers.CollectionSearchResolver;
import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.search.Edge;
import com.circumgraph.storage.search.PageCursor;
import com.circumgraph.storage.search.PageCursors;
import com.circumgraph.storage.search.PageInfo;
import com.circumgraph.storage.search.QueryPath;
import com.circumgraph.storage.search.SearchResult;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

/**
 * Generator for a search query within the GraphQL schema.
 */
public class SearchQueryGenerator
{
	private static final FieldResolver TOTAL_COUNT_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getTotalCount();
	};

	private static final FieldResolver TOTAL_COUNT_ESTIMATED_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.isTotalCountEstimated();
	};

	private static final FieldResolver PAGE_INFO_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getPageInfo();
	};

	private static final FieldResolver PAGE_CURSORS_FETCHER = (env) -> {
		var max = env.getArgumentOrDefault("max", 11);
		SearchResult ctx = env.getSource();
		return ctx.getPageCursors(max);
	};

	private static final FieldResolver EDGES_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getEdges();
	};

	private static final FieldResolver NODES_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getNodes();
	};

	private static final FieldResolver EDGE_SCORE_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return ctx.getScore();
	};

	private static final FieldResolver EDGE_CURSOR_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return CursorEncoding.encode(ctx.getCursor());
	};

	private static final FieldResolver EDGE_NODE_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return ctx.getNode();
	};

	private final MutableMap<String, Criteria> indexerToCriteria;

	private final OutputTypeDef pageInfoType;
	private final OutputTypeDef pageCursorsType;

	public SearchQueryGenerator()
	{
		indexerToCriteria = Maps.mutable.empty();
		indexerToCriteria.put("ID", new IDCriteria());
		indexerToCriteria.put("INT", new IntCriteria());
		indexerToCriteria.put("FLOAT", new FloatCriteria());
		indexerToCriteria.put("BOOLEAN", new BooleanCriteria());

		indexerToCriteria.put("TOKEN", new StringTokenCriteria());

		var fullText = new StringFullTextCriteria();
		indexerToCriteria.put("FULL_TEXT", fullText);
		indexerToCriteria.put("TYPE_AHEAD", fullText);

		indexerToCriteria.put("LOCAL_DATE", new LocalDateCriteria());
		indexerToCriteria.put("LOCAL_TIME", new LocalTimeCriteria());

		this.pageInfoType = generatePageInfo();
		this.pageCursorsType = generatePageCursors();
	}

	private OutputTypeDef generatePageInfo()
	{
		return ObjectDef.create("PageInfo")
			.withDescription("Information about the current page")
			.addField(FieldDef.create("hasNextPage")
				.withType(NonNullDef.output(ScalarDef.BOOLEAN))
				.withDescription("If there is a page available after this one")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageInfo pageInfo = env.getSource();
					return pageInfo.hasNextPage();
				})
				.build()
			)
			.addField(FieldDef.create("hasPreviousPage")
				.withType(NonNullDef.output(ScalarDef.BOOLEAN))
				.withDescription("If there is a page available before this one")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageInfo pageInfo = env.getSource();
					return pageInfo.hasPreviousPage();
				})
				.build()
			)
			.addField(FieldDef.create("startCursor")
				.withType(NonNullDef.output(ScalarDef.STRING))
				.withDescription("Cursor representing the start of the result")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageInfo pageInfo = env.getSource();
					return CursorEncoding.encode(pageInfo.getStartCursor());
				})
				.build()
			)
			.addField(FieldDef.create("endCursor")
				.withType(NonNullDef.output(ScalarDef.STRING))
				.withDescription("Cursor representing the end of the result")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageInfo pageInfo = env.getSource();
					return CursorEncoding.encode(pageInfo.getEndCursor());
				})
				.build()
			)
			.build();
	}

	private OutputTypeDef generatePageCursors()
	{
		var pageCursorType = ObjectDef.create("PageCursor")
			.withDescription("Cursor information for a specific page")
			.addField(FieldDef.create("cursor")
				.withType(NonNullDef.output(ScalarDef.STRING))
				.withDescription("Cursor for this page")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursor pageCursor = env.getSource();
					return CursorEncoding.encode(pageCursor.getCursor());
				})
				.build()
			)
			.addField(FieldDef.create("pageNumber")
				.withType(NonNullDef.output(ScalarDef.INT))
				.withDescription("Page number")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursor pageCursor = env.getSource();
					return pageCursor.getPageNumber();
				})
				.build()
			)
			.addField(FieldDef.create("current")
				.withType(NonNullDef.output(ScalarDef.BOOLEAN))
				.withDescription("If this is the current page")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursor pageCursor = env.getSource();
					return pageCursor.isCurrent();
				})
				.build()
			)
			.build();

		return ObjectDef.create("PageCursors")
			.withDescription("""
				Detailed information about page cursors, used to generate
				pagination.

				Can be used to fetch a cursor to the `previous` or `next` page.
				Or to create a full pagination bar via the lists returned from
				the `start`, `middle` and `end` fields.

				Cursors in this object should be used given to search as the
				`after` argument with the same `first` argument.
			""")
			.addField(FieldDef.create("previous")
				.withType(pageCursorType)
				.withDescription("Get the previous page if available")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursors pageCursors = env.getSource();
					return pageCursors.getPrevious();
				})
				.build()
			)
			.addField(FieldDef.create("start")
				.withType(NonNullDef.output(ListDef.output(
					NonNullDef.output(pageCursorType)
				)))
				.withDescription("Pages at the start of the pagination")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursors pageCursors = env.getSource();
					return pageCursors.getStart();
				})
				.build()
			)
			.addField(FieldDef.create("middle")
				.withType(NonNullDef.output(ListDef.output(
					NonNullDef.output(pageCursorType)
				)))
				.withDescription("Pages at the middle of the pagination")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursors pageCursors = env.getSource();
					return pageCursors.getMiddle();
				})
				.build()
			)
			.addField(FieldDef.create("end")
				.withType(NonNullDef.output(ListDef.output(
					NonNullDef.output(pageCursorType)
				)))
				.withDescription("Pages at the end of the pagination")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursors pageCursors = env.getSource();
					return pageCursors.getEnd();
				})
				.build()
			)
			.addField(FieldDef.create("next")
				.withType(pageCursorType)
				.withDescription("Get the next page if available")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> {
					PageCursors pageCursors = env.getSource();
					return pageCursors.getNext();
				})
				.build()
			)
			.build();
	}

	public FieldDef generateSearchQuery(
		StructuredDef def,
		String fieldName
	)
	{
		var resultType = generateResultType(def);
		var criteria = generateCriteria(def, false);

		var fieldBuilder = FieldDef.create(fieldName)
			.withType(resultType)
			.withDescription("Search for " + def.getName())
			.withMetadata(GraphQLModel.FIELD_RESOLVER_FACTORY, new CollectionSearchResolver(def, criteria))
			.addArgument(ArgumentDef.create("first")
				.withType(ScalarDef.INT)
				.withDescription("Return the first N results from the start or from the cursor given by `after`")
				.build()
			)
			.addArgument(ArgumentDef.create("after")
				.withType(ScalarDef.STRING)
				.withDescription("Return results after the given cursor")
				.build()
			)
			.addArgument(ArgumentDef.create("criteria")
				.withType(ListDef.input(NonNullDef.input(criteria.getGraphQLType())))
				.withDescription("Criteria that should match, leave null or empty to match everything")
				.build()
			);

		var sortInput = generateSortInput(def);
		if(sortInput != null)
		{
			fieldBuilder = fieldBuilder.addArgument(ArgumentDef.create("sort")
				.withType(ListDef.input(NonNullDef.input(sortInput)))
				.withDescription("Sort the result of this search")
				.build()
			);
		}

		return fieldBuilder.build();
	}

	private Criteria generateCriteria(OutputTypeDef output, boolean allowReferences)
	{
		if(output instanceof NonNullDef.Output nonNullDef)
		{
			output = nonNullDef.getType();
		}

		// TODO: Caching
		// TODO: Recursion

		if(output instanceof StructuredDef structuredDef)
		{
			if(allowReferences && structuredDef.findImplements(StorageSchema.ENTITY_NAME))
			{
				return new StoredObjectRefCriteria(
					structuredDef,
					(IDCriteria) indexerToCriteria.get("ID")
				);
			}

			return new StructuredDefCriteria(
				structuredDef,
				resolveFieldCriteria(structuredDef),
				generateSubCriteria(structuredDef)
			);
		}
		else if(output instanceof EnumDef enumDef)
		{
			return new EnumCriteria(enumDef);
		}
		else if(output instanceof UnionDef unionDef)
		{
			return new UnionCriteria(
				unionDef,
				generateSubCriteria(unionDef)
			);
		}

		// No querying available
		return null;
	}

	private FieldCriteria resolveFieldCriteria(StructuredDef def)
	{
		var fields = def.getFields()
			.toMap(FieldDef::getName, f -> {
				var indexer = StorageModel.getIndexer(f);
				if(indexer.isPresent())
				{
					// Field has an indexer - use the Criteria associated with it
					var criteria = indexerToCriteria.get(indexer.get().getName());
					if(criteria != null)
					{
						return criteria;
					}

					return generateCriteria(f.getType(), true);
				}
				else if(StorageModel.isIndexed(f))
				{
					// For other fields we try to resolve a criteria
					return generateCriteria(f.getType(), true);
				}

				return null;
			})
			.select((key, v) -> v != null)
			.toImmutable();

		if(fields.isEmpty())
		{
			// If there are no indexed fields we can't generate a criteria
			return null;
		}

		return new FieldCriteria(def, fields);
	}

	private ImmutableMap<String, StructuredDefCriteria> generateSubCriteria(StructuredDef def)
	{
		if(! (def instanceof InterfaceDef i)) return Maps.immutable.empty();

		var result = Maps.mutable.<String, StructuredDefCriteria>empty();

		for(var subDef : i.getImplementors())
		{
			result.put(
				SchemaNames.toQueryFieldName(subDef),
				(StructuredDefCriteria) generateCriteria(subDef, false)
			);
		}

		return result.toImmutable();
	}

	private ImmutableMap<String, Criteria> generateSubCriteria(UnionDef def)
	{
		var result = Maps.mutable.<String, Criteria>empty();

		for(var subDef : def.getTypes())
		{
			result.put(
				SchemaNames.toQueryFieldName(subDef),
				generateCriteria(subDef, true)
			);
		}

		return result.toImmutable();
	}

	private InputObjectDef generateSortInput(
		StructuredDef def
	)
	{
		var rootPath = QueryPath.root(def);

		var values = def.getFields()
			.select(f -> StorageModel.isSortable(f))
			.collect(field -> EnumValueDef.create(SchemaNames.toSortEnumValue(field))
				.withDescription("Sort by the field " + field.getName())
				.withMetadata(GraphQLModel.ENUM_VALUE, rootPath.field(field.getName()))
				.build()
			)
			.toList();

		if(values.isEmpty())
		{
			// If there are no sortable fields skip sort ability
			return null;
		}

		var enumType = EnumDef.create(SchemaNames.toSortEnumName(def))
			.addValues(values)
			.build();

		return InputObjectDef.create(SchemaNames.toSortInputName(def))
			.withDescription("Input used to define sorting when querying for " + def.getName())
			.addField(InputFieldDef.create("field")
				.withType(NonNullDef.input(enumType))
				.withDescription("The field to sort on")
				.build()
			)
			.addField(InputFieldDef.create("ascending")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("If the sort should be ascending, `true` by default")
				.withDefaultValue(true)
				.build()
			)
			.build();
	}

	private ObjectDef generateResultType(
		StructuredDef def
	)
	{
		var name = SchemaNames.toSearchResultTypeName(def);
		return ObjectDef.create(name)
			.withDescription("Search result for " + def.getName() + ".")
			.addField(FieldDef.create("totalCount")
				.withType(NonNullDef.output(ScalarDef.INT))
				.withDescription("Total items matching")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, TOTAL_COUNT_FETCHER)
				.build()
			)
			.addField(FieldDef.create("totalCountEstimated")
				.withType(NonNullDef.output(ScalarDef.BOOLEAN))
				.withDescription("If the total count was estimated")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, TOTAL_COUNT_ESTIMATED_FETCHER)
				.build()
			)
			.addField(FieldDef.create("pageInfo")
				.withType(NonNullDef.output(pageInfoType))
				.withDescription("Information about the current page")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, PAGE_INFO_FETCHER)
				.build()
			)
			.addField(FieldDef.create("pageCursors")
				.withType(NonNullDef.output(pageCursorsType))
				.withDescription("Generate pagination cursors")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, PAGE_CURSORS_FETCHER)
				.addArgument(ArgumentDef.create("max")
					.withType(ScalarDef.INT)
					.withDescription("The maximum number of pages to display, includes separators")
					.build()
				)
				.build()
			)
			.addField(FieldDef.create("nodes")
				.withType(NonNullDef.output(ListDef.output(NonNullDef.output(def.getName()))))
				.withDescription("Direct access to the matching items")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, NODES_FETCHER)
				.build()
			)
			.addField(FieldDef.create("edges")
				.withType(NonNullDef.output(ListDef.output(NonNullDef.output(generateEdgeType(def)))))
				.withDescription("Matching items including scores and cursors")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, EDGES_FETCHER)
				.build()
			)
			.build();
	}

	private ObjectDef generateEdgeType(
		StructuredDef def
	)
	{
		var name = SchemaNames.toSearchEdgeTypeName(def);

		return ObjectDef.create(name)
			.withDescription("Edge for " + def.getName() + ".")
			.addField(FieldDef.create("score")
				.withType(NonNullDef.output(ScalarDef.FLOAT))
				.withDescription("Score of this result")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, EDGE_SCORE_FETCHER)
				.build()
			)
			.addField(FieldDef.create("cursor")
				.withType(NonNullDef.output(ScalarDef.STRING))
				.withDescription("Cursor for this result, can be used to return results before or after this")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, EDGE_CURSOR_FETCHER)
				.build()
			)
			.addField(FieldDef.create("node")
				.withType(NonNullDef.output(def.getName()))
				.withDescription("The matching result")
				.withMetadata(GraphQLModel.FIELD_RESOLVER, EDGE_NODE_FETCHER)
				.build()
			)
			.build();
	}
}
