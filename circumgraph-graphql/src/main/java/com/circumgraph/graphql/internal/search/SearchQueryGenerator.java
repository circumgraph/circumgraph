package com.circumgraph.graphql.internal.search;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.search.Edge;
import com.circumgraph.storage.search.Page;
import com.circumgraph.storage.search.PageCursor;
import com.circumgraph.storage.search.PageCursors;
import com.circumgraph.storage.search.PageInfo;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;
import com.circumgraph.storage.search.SearchResult;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import se.l4.silo.index.FieldSort;
import se.l4.ylem.ids.LongIdCodec;

/**
 * Generator for a search query within the GraphQL schema.
 */
public class SearchQueryGenerator
{
	private static final DataFetcher<?> TOTAL_COUNT_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getTotalCount();
	};

	private static final DataFetcher<?> TOTAL_COUNT_ESTIMATED_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.isTotalCountEstimated();
	};

	private static final DataFetcher<?> PAGE_INFO_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getPageInfo();
	};

	private static final DataFetcher<?> PAGE_CURSORS_FETCHER = (env) -> {
		var max = env.getArgumentOrDefault("max", 11);
		SearchResult ctx = env.getSource();
		return ctx.getPageCursors(max);
	};

	private static final DataFetcher<?> EDGES_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getEdges();
	};

	private static final DataFetcher<?> NODES_FETCHER = (env) -> {
		SearchResult ctx = env.getSource();
		return ctx.getNodes();
	};

	private static final DataFetcher<?> EDGE_SCORE_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return ctx.getScore();
	};

	private static final DataFetcher<?> EDGE_CURSOR_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return CursorEncoding.encode(ctx.getCursor());
	};

	private static final DataFetcher<?> EDGE_NODE_FETCHER = (env) -> {
		Edge ctx = env.getSource();
		return ctx.getNode();
	};

	private final GraphQLCodeRegistry.Builder codeRegistry;

	private final MutableMap<String, Criteria> indexerToCriteria;

	private final GraphQLOutputType pageInfoType;
	private final GraphQLOutputType pageCursorsType;

	public SearchQueryGenerator(
		GraphQLCodeRegistry.Builder codeRegistry,
		LongIdCodec<String> idCodec
	)
	{
		this.codeRegistry = codeRegistry;
		indexerToCriteria = Maps.mutable.empty();
		indexerToCriteria.put("ID", new IDCriteria(idCodec));
		indexerToCriteria.put("INT", new IntCriteria());
		indexerToCriteria.put("FLOAT", new FloatCriteria());
		indexerToCriteria.put("BOOLEAN", new BooleanCriteria());

		indexerToCriteria.put("TOKEN", new StringTokenCriteria());

		var fullText = new StringFullTextCriteria();
		indexerToCriteria.put("FULL_TEXT", fullText);
		indexerToCriteria.put("TYPE_AHEAD", fullText);

		this.pageInfoType = generatePageInfo();
		this.pageCursorsType = generatePageCursors();
	}

	private GraphQLOutputType generatePageInfo()
	{
		var type = GraphQLObjectType.newObject()
			.name("PageInfo")
			.description("Information about the current page")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("hasNextPage")
				.description("If there is a page available after this one")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("hasPreviousPage")
				.description("If there is a page available before this one")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("startCursor")
				.description("Cursor representing the start of the result")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("endCursor")
				.description("Cursor representing the end of the result")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
			)
			.build();

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageInfo", "hasNextPage"),
			(DataFetchingEnvironment env) -> {
				PageInfo pageInfo = env.getSource();
				return pageInfo.hasNextPage();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageInfo", "hasPreviousPage"),
			(DataFetchingEnvironment env) -> {
				PageInfo pageInfo = env.getSource();
				return pageInfo.hasPreviousPage();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageInfo", "startCursor"),
			(DataFetchingEnvironment env) -> {
				PageInfo pageInfo = env.getSource();
				return CursorEncoding.encode(pageInfo.getStartCursor());
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageInfo", "endCursor"),
			(DataFetchingEnvironment env) -> {
				PageInfo pageInfo = env.getSource();
				return CursorEncoding.encode(pageInfo.getEndCursor());
			}
		);

		return type;
	}

	private GraphQLOutputType generatePageCursors()
	{
		var pageCursorType = GraphQLObjectType.newObject()
			.name("PageCursor")
			.description("Cursor information for a specific page")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("cursor")
				.description("Cursor for this page")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("pageNumber")
				.description("Page number")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLInt))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("current")
				.description("If this is the current page")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean))
			)
			.build();

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursor", "cursor"),
			(DataFetchingEnvironment env) -> {
				PageCursor pageCursor = env.getSource();
				return CursorEncoding.encode(pageCursor.getCursor());
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursor", "pageNumber"),
			(DataFetchingEnvironment env) -> {
				PageCursor pageCursor = env.getSource();
				return pageCursor.getPageNumber();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursor", "current"),
			(DataFetchingEnvironment env) -> {
				PageCursor pageCursor = env.getSource();
				return pageCursor.isCurrent();
			}
		);

		var type = GraphQLObjectType.newObject()
			.name("PageCursors")
			.description("""
				Detailed information about page cursors, used to generate
				pagination.

				Can be used to fetch a cursor to the `previous` or `next` page.
				Or to create a full pagination bar via the lists returned from
				the `start`, `middle` and `end` fields.

				Cursors in this object should be used given to search as the
				`after` argument with the same `first` argument.
			""")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("previous")
				.description("Get the previous page if available")
				.type(pageCursorType)
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("start")
				.description("Pages at the start of the pagination")
				.type(GraphQLNonNull.nonNull(GraphQLList.list(
					(GraphQLNonNull.nonNull(pageCursorType)
				))))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("middle")
				.description("Pages at the middle of the pagination")
				.type(GraphQLNonNull.nonNull(GraphQLList.list(
					(GraphQLNonNull.nonNull(pageCursorType)
				))))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("end")
				.description("Pages at the start of the pagination")
				.type(GraphQLNonNull.nonNull(GraphQLList.list(
					(GraphQLNonNull.nonNull(pageCursorType)
				))))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("next")
				.description("Get the next page if available")
				.type(pageCursorType)
			)
			.build();

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursors", "previous"),
			(DataFetchingEnvironment env) -> {
				PageCursors pageCursors = env.getSource();
				return pageCursors.getPrevious();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursors", "start"),
			(DataFetchingEnvironment env) -> {
				PageCursors pageCursors = env.getSource();
				return pageCursors.getStart();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursors", "middle"),
			(DataFetchingEnvironment env) -> {
				PageCursors pageCursors = env.getSource();
				return pageCursors.getMiddle();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursors", "end"),
			(DataFetchingEnvironment env) -> {
				PageCursors pageCursors = env.getSource();
				return pageCursors.getEnd();
			}
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates("PageCursors", "next"),
			(DataFetchingEnvironment env) -> {
				PageCursors pageCursors = env.getSource();
				return pageCursors.getNext();
			}
		);

		return type;
	}

	public void generateSearchQuery(
		Collection collection,
		String queryName,
		GraphQLObjectType.Builder builder
	)
	{
		var def = collection.getDefinition();
		var resultType = generateResultType(def);
		var criteria = generateCriteria(def);

		var fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
			.name("search")
			.description("Search for " + def.getName())
			.type(resultType)
			.argument(GraphQLArgument.newArgument()
				.name("first")
				.description("Return the first N results from the start or from the cursor given by `after`")
				.type(Scalars.GraphQLInt)
			)
			.argument(GraphQLArgument.newArgument()
				.name("after")
				.description("Return results after the given cursor")
				.type(Scalars.GraphQLString)
			)
			.argument(GraphQLArgument.newArgument()
				.name("criteria")
				.description("Criteria that should match, leave null or empty to match everything")
				.type(GraphQLList.list(GraphQLNonNull.nonNull(criteria.getGraphQLType())))
			);

		var sortInput = generateSortInput(def);
		if(sortInput != null)
		{
			fieldBuilder.argument(GraphQLArgument.newArgument()
				.name("sort")
				.description("Sort the result of this search")
				.type(GraphQLList.list(GraphQLNonNull.nonNull(generateSortInput(def))))
			);
		}

		builder.field(fieldBuilder.build());

		DataFetcher<CompletableFuture<SearchResult>> dataFetcher = (env) -> {
			var query = Query.create();

			if(env.containsArgument("criteria"))
			{
				List<Map<String, Object>> args = env.getArgument("criteria");
				var path = QueryPath.root(def);
				for(var e : args)
				{
					query = query.addClause(criteria.toClause(e, path));
				}
			}

			if(env.containsArgument("sort"))
			{
				List<Map<String, Object>> sort = env.getArgument("sort");
				if(! sort.isEmpty())
				{
					for(var s : sort)
					{
						query = query.addSort(FieldSort.create(
							(String) s.get("field"),
							(Boolean) s.get("ascending")
						));
					}
				}
			}

			query = query.withPage(Page.first(
				env.getArgumentOrDefault("first", 10),
				CursorEncoding.decode(env.getArgument("after"))
			));

			// Check if scores are being fetched
			query = query.withScoresNeeded(
				env.getSelectionSet().contains("edges/score")
			);

			StorageContext ctx = env.getContext();
			return ctx.getTx()
				.wrap(collection.search(query))
				.toFuture();
		};

		codeRegistry.dataFetcher(FieldCoordinates.coordinates(queryName, "search"), dataFetcher);
	}

	private Criteria generateCriteria(OutputTypeDef output)
	{
		if(output instanceof StructuredDef)
		{
			var structuredDef = (StructuredDef) output;
			return new StructuredDefCriteria(
				structuredDef,
				resolveFieldCriteria(structuredDef),
				Maps.immutable.empty()
			);
		}

		throw new RuntimeException();
	}

	private FieldCriteria resolveFieldCriteria(StructuredDef def)
	{
		var fields = def.getFields()
			.select(f -> StorageModel.getIndexerType(f).isPresent())
			.toMap(FieldDef::getName, f -> {
				var indexer = StorageModel.getIndexerType(f);
				return indexerToCriteria.get(indexer.get());
			})
			.toImmutable();

		return new FieldCriteria(def, fields);
	}

	private GraphQLInputObjectType generateSortInput(
		StructuredDef def
	)
	{
		var values = def.getFields()
			.select(f -> StorageModel.isSortable(f))
			.collect(field -> GraphQLEnumValueDefinition.newEnumValueDefinition()
				.name(field.getName().toString().toUpperCase()) // TODO should be upper snake case
				.description("Sort by the field " + field.getName())
				.value("_." + field.getName())
				.build()
			)
			.toList();

		if(values.isEmpty())
		{
			// If there are no sortable fields skip sort ability
			return null;
		}

		var enumType = GraphQLEnumType.newEnum()
			.name(def.getName() + "Sort")
			.values(values)
			.build();

		return GraphQLInputObjectType.newInputObject()
			.name(def.getName() + "SortInput")
			.description("Input used to define sorting when querying for " + def.getName())
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("field")
				.description("The field to sort one")
				.type(GraphQLNonNull.nonNull(enumType))
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("ascending")
				.description("If the sort should be ascending, `true` by default")
				.type(Scalars.GraphQLBoolean)
				.defaultValue(true)
			)
			.build();
	}

	private GraphQLObjectType generateResultType(
		StructuredDef def
	)
	{
		var name = def.getName() + "SearchResult";

		var type = GraphQLObjectType.newObject()
			.name(name)
			.description("Search result for " + def.getName() + ".")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("totalCount")
				.description("Total items matching")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLInt))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("totalCountEstimated")
				.description("If the total count was estimated")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("pageInfo")
				.description("Information about the current page")
				.type(GraphQLNonNull.nonNull(pageInfoType))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("pageCursors")
				.description("Generate pagination cursors")
				.type(GraphQLNonNull.nonNull(pageCursorsType))
				.argument(GraphQLArgument.newArgument()
					.name("max")
					.description("The maximum number of pages to display, includes separators")
					.type(Scalars.GraphQLInt)
				)
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("nodes")
				.description("Direct access to the matching items")
				.type(GraphQLNonNull.nonNull(GraphQLList.list(
					GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(def.getName()))
				)))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("edges")
				.description("Matching items including scores and cursors")
				.type(GraphQLNonNull.nonNull(GraphQLList.list(
					GraphQLNonNull.nonNull(generateEdgeType(def))
				)))
			)
			.build();

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "totalCount"),
			TOTAL_COUNT_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "totalCountEstimated"),
			TOTAL_COUNT_ESTIMATED_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "pageInfo"),
			PAGE_INFO_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "pageCursors"),
			PAGE_CURSORS_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "nodes"),
			NODES_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "edges"),
			EDGES_FETCHER
		);

		return type;
	}

	private GraphQLObjectType generateEdgeType(
		StructuredDef def
	)
	{
		var name = def.getName() + "Edge";

		var type = GraphQLObjectType.newObject()
			.name(name)
			.description("Edge for " + def.getName() + ".")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("score")
				.description("Score of this result")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLFloat))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("cursor")
				.description("Cursor for this result, can be used to return results before or after this")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("node")
				.description("The matching result")
				.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(def.getName())))
			)
			.build();

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "score"),
			EDGE_SCORE_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "cursor"),
			EDGE_CURSOR_FETCHER
		);

		codeRegistry.dataFetcher(
			FieldCoordinates.coordinates(name, "node"),
			EDGE_NODE_FETCHER
		);

		return type;
	}
}
