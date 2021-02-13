package com.circumgraph.schema.graphql;

import java.io.Reader;
import java.io.StringReader;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import graphql.language.DescribedNode;
import graphql.language.EnumTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SDLDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;

/**
 * Parsing of a GraphQL schema.
 */
public class GraphQLSchema
	implements Schema
{
	private final Iterable<? extends TypeDef> types;

	public GraphQLSchema(
		Iterable<? extends TypeDef> types
	)
	{
		this.types = types;
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return types;
	}

	/**
	 * Create a schema from the given string.
	 *
	 * @param schema
	 * @return
	 */
	public static GraphQLSchema create(String schema)
	{
		return create(new StringReader(schema));
	}

	/**
	 * Create a schema from the given {@link Reader}.
	 *
	 * @param reader
	 * @return
	 */
	public static GraphQLSchema create(Reader reader)
	{
		MutableList<TypeDef> types = Lists.mutable.empty();

		TypeDefinitionRegistry registry;
		try
		{
			SchemaParser schemaParser = new SchemaParser();
			registry = schemaParser.parse(reader);
		}
		catch(SchemaProblem e)
		{
			throw new ModelException("Unable to parse schema; " + e.getMessage(), e);
		}

		for(TypeDefinition<?> type : registry.types().values())
		{
			if(type instanceof ObjectTypeDefinition)
			{
				types.add(defineObject((ObjectTypeDefinition) type));
			}
			else if(type instanceof InterfaceTypeDefinition)
			{
				types.add(defineInterface((InterfaceTypeDefinition) type));
			}
			else if(type instanceof EnumTypeDefinition)
			{
				types.add(defineEnum((EnumTypeDefinition) type));
			}
		}

		return new GraphQLSchema(types);
	}

	private static ObjectDef defineObject(ObjectTypeDefinition def)
	{
		return ObjectDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addImplementsAll(createImplements(def.getImplements()))
			.addFields(createFields(def.getFieldDefinitions()))
			.build();
	}

	private static InterfaceDef defineInterface(InterfaceTypeDefinition def)
	{
		return InterfaceDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addImplementsAll(createImplements(def.getImplements()))
			.addFields(createFields(def.getFieldDefinitions()))
			.build();
	}

	private static EnumDef defineEnum(EnumTypeDefinition def)
	{
		return EnumDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addValues(Lists.immutable.ofAll(def.getEnumValueDefinitions())
				.collect(v -> EnumValueDef.create(v.getName())
					.withDescription(toDescription(v))
					.build()
				)
			)
			.build();
	}

	private static Iterable<String> createImplements(
		Iterable<Type> types
	)
	{
		return Lists.immutable.ofAll(types)
			.collect(t -> ((TypeName) t).getName());
	}

	private static Iterable<FieldDef> createFields(
		Iterable<FieldDefinition> fields
	)
	{
		return Lists.immutable.ofAll(fields)
			.collect(GraphQLSchema::defineField);
	}

	private static FieldDef defineField(FieldDefinition def)
	{
		graphql.language.Type<?> gqlType = def.getType();
		boolean nullable;

		if(gqlType instanceof NonNullType)
		{
			nullable = false;
			gqlType = ((NonNullType) gqlType).getType();
		}
		else
		{
			nullable = true;
		}

		OutputTypeDef dataType = toOutputType(gqlType);

		return FieldDef.create(def.getName())
			.withDescription(def.getDescription() == null ? null : def.getDescription().content)
			.withNullable(nullable)
			.withType(dataType)
			.build();
	}

	private static OutputTypeDef toOutputType(graphql.language.Type<?> type)
	{
		if(type instanceof ListType)
		{
			return ListDef.output(toOutputType(type));
		}

		TypeName tn = (TypeName) type;
		return TypeRef.create(tn.getName());
	}

	private static String toDescription(DescribedNode<?> node)
	{
		return node.getDescription() == null
			? null
			: node.getDescription().getContent();
	}

	private static SourceLocation toSourceLocation(SDLDefinition<?> def)
	{
		graphql.language.SourceLocation loc = def.getSourceLocation();
		if(loc == null) return SourceLocation.unknown();

		return SourceLocation.create(
			loc.getSourceName() == null ? "<source>" : loc.getSourceName()
			+ ":" + loc.getLine() + ":" + loc.getColumn()
		);
	}
}
