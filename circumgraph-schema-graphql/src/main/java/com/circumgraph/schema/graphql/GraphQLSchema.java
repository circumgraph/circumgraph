package com.circumgraph.schema.graphql;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.ModelValidationException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.DescribedNode;
import graphql.language.DirectivesContainer;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValue;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.Node;
import graphql.language.NonNullType;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;

/**
 * Parsing of a GraphQL schema.
 */
public class GraphQLSchema
	implements Schema
{
	private static final ValidationMessageType SYNTAX_ERROR = ValidationMessageType.error()
		.withCode("schema:graphql:parse-error")
		.withArgument("message")
		.withMessage("{{message}}")
		.build();

	private static final ValidationMessageType UNSUPPORTED_TYPE = ValidationMessageType.error()
		.withCode("schema:graphql:unsupported-type")
		.withArgument("type")
		.withMessage("{{type}} was defined, but its underlying type is currently unsupported")
		.build();

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
			throw new ModelValidationException(
				"Unable to parse schema:",
				Lists.immutable.ofAll(e.getErrors())
					.collect(err -> SYNTAX_ERROR.toMessage()
						.withLocation(toSourceLocation(err.getLocations()))
						.withArgument("message", err.getMessage())
						.build()
					)
			);
		}

		MutableList<ValidationMessage> messages = Lists.mutable.empty();

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
			else if(type instanceof UnionTypeDefinition)
			{
				types.add(defineUnion((UnionTypeDefinition) type));
			}
			else
			{
				messages.add(UNSUPPORTED_TYPE.toMessage()
					.withLocation(toSourceLocation(type))
					.withArgument("type", type.getName())
					.build()
				);
			}
		}

		// Handle extensions
		handleExtension(types, registry.interfaceTypeExtensions(), GraphQLSchema::defineInterface);
		handleExtension(types, registry.objectTypeExtensions(), GraphQLSchema::defineObject);
		handleExtension(types, registry.unionTypeExtensions(), GraphQLSchema::defineUnion);
		handleExtension(types, registry.enumTypeExtensions(), GraphQLSchema::defineEnum);

		if(messages.anySatisfy(ValidationMessage.errorPredicate()))
		{
			// Schema has errors, throw a validation message
			throw new ModelValidationException(messages);
		}

		return new GraphQLSchema(types);
	}

	private static <V> void handleExtension(
		MutableList<TypeDef> types,
		Map<String, List<V>> items,
		Function<V, ? extends TypeDef> f)
	{
		for(var extension : items.values())
		{
			for(var type : extension)
			{
				types.add(f.apply(type));
			}
		}
	}

	private static ObjectDef defineObject(ObjectTypeDefinition def)
	{
		return ObjectDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addDirectives(createDirectives(def))
			.addImplementsAll(createImplements(def.getImplements()))
			.addFields(createFields(def.getFieldDefinitions()))
			.build();
	}

	private static InterfaceDef defineInterface(InterfaceTypeDefinition def)
	{
		return InterfaceDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addDirectives(createDirectives(def))
			.addImplementsAll(createImplements(def.getImplements()))
			.addFields(createFields(def.getFieldDefinitions()))
			.build();
	}

	private static EnumDef defineEnum(EnumTypeDefinition def)
	{
		return EnumDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addDirectives(createDirectives(def))
			.addValues(Lists.immutable.ofAll(def.getEnumValueDefinitions())
				.collect(v -> EnumValueDef.create(v.getName())
					.withDescription(toDescription(v))
					.build()
				)
			)
			.build();
	}

	private static UnionDef defineUnion(UnionTypeDefinition def)
	{
		return UnionDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.addDirectives(createDirectives(def))
			.addTypes(Lists.immutable.ofAll(def.getMemberTypes())
				.collect(type -> toOutputType(type))
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
		OutputTypeDef dataType = toOutputType(gqlType);

		return FieldDef.create(def.getName())
			.withSourceLocation(toSourceLocation(def))
			.withDescription(toDescription(def))
			.withType(dataType)
			.addArguments(createArguments(def))
			.addDirectives(createDirectives(def))
			.build();
	}

	private static Iterable<ArgumentDef> createArguments(FieldDefinition def)
	{
		return Lists.immutable.ofAll(def.getInputValueDefinitions())
			.collect(arg -> {
				graphql.language.Type<?> gqlType = def.getType();
				InputTypeDef dataType = toInputType(gqlType);

				return ArgumentDef.create(arg.getName())
					.withSourceLocation(toSourceLocation(def))
					.withDescription(toDescription(def))
					.withType(dataType)
					.addDirectives(createDirectives(arg))
					.build();
			});
	}

	private static Iterable<DirectiveUse> createDirectives(
		DirectivesContainer<?> def
	)
	{
		return Lists.immutable.ofAll(def.getDirectives())
			.collect(d -> {
				var builder = DirectiveUse.create(d.getName())
					.withSourceLocation(toSourceLocation(d));

				for(var arg : d.getArguments())
				{
					builder = builder.addArgument(arg.getName(), toJavaValue(arg.getValue()));
				}

				return builder.build();
			});
	}

	private static OutputTypeDef toOutputType(graphql.language.Type<?> type)
	{
		if(type instanceof NonNullType)
		{
			return NonNullDef.output(
				toOutputType(((NonNullType) type).getType())
			);
		}
		else if(type instanceof ListType)
		{
			return ListDef.output(toOutputType(
				((ListType) type).getType()
			));
		}

		TypeName tn = (TypeName) type;
		return TypeRef.create(tn.getName());
	}

	private static InputTypeDef toInputType(graphql.language.Type<?> type)
	{
		if(type instanceof NonNullType)
		{
			return NonNullDef.input(
				toInputType(((NonNullType) type).getType())
			);
		}
		else if(type instanceof ListType)
		{
			return ListDef.input(toInputType(
				((ListType) type).getType()
			));
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

	private static SourceLocation toSourceLocation(
		List<graphql.language.SourceLocation> locations
	)
	{
		return locations.isEmpty()
			? SourceLocation.unknown()
			: toSourceLocation(locations.get(0));
	}

	private static SourceLocation toSourceLocation(Node<?> def)
	{
		return toSourceLocation(def.getSourceLocation());
	}

	private static SourceLocation toSourceLocation(graphql.language.SourceLocation loc)
	{
		if(loc == null) return SourceLocation.unknown();

		return SourceLocation.create(
			(loc.getSourceName() == null ? "<source>" : loc.getSourceName())
			+ ":" + loc.getLine() + ":" + loc.getColumn()
		);
	}

	private static Object toJavaValue(Value<?> input)
	{
		if(input instanceof NullValue)
		{
			return null;
		}
		else if(input instanceof FloatValue)
		{
			return ((FloatValue) input).getValue();
		}
		else if(input instanceof StringValue)
		{
			return ((StringValue) input).getValue();
		}
		else if (input instanceof IntValue)
		{
			return ((IntValue) input).getValue().intValue();
		}
		else if (input instanceof BooleanValue)
		{
			return ((BooleanValue) input).isValue();
		}
		else if (input instanceof EnumValue)
		{
			return ((EnumValue) input).getName();
		}
		else if(input instanceof ArrayValue)
		{
			return Lists.immutable.ofAll(((ArrayValue) input).getValues())
				.collect(GraphQLSchema::toJavaValue);
		}
		else if(input instanceof ObjectValue)
		{
			List<ObjectField> values = ((ObjectValue) input).getObjectFields();
			MutableMap<String, Object> parsedValues = Maps.mutable.empty();
			values.forEach(field -> {
				Object parsedValue = toJavaValue(field.getValue());
				parsedValues.put(field.getName(), parsedValue);
			});
			return parsedValues.toImmutable();
		}

		throw new CoercingParseLiteralException();
	}
}
