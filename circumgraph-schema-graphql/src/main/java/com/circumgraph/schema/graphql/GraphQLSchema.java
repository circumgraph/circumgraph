package com.circumgraph.schema.graphql;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

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
import graphql.language.Definition;
import graphql.language.DescribedNode;
import graphql.language.DirectivesContainer;
import graphql.language.Document;
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
import graphql.parser.InvalidSyntaxException;
import graphql.parser.MultiSourceReader;
import graphql.parser.Parser;
import graphql.schema.CoercingParseLiteralException;

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
	 *   schema SDL
	 * @return
	 *   parsed schema
	 * @throws ModelValidationException
	 *   if schema is not valid
	 */
	public static GraphQLSchema create(String schema)
	{
		return create(new StringReader(schema));
	}

	/**
	 * Create a schema from the given string.
	 *
	 * @param schema
	 *   schema SDL
	 * @param sourceName
	 *   name of source, commonly a filename
	 * @return
	 *   parsed schema
	 * @throws ModelValidationException
	 *   if schema is not valid
	 */
	public static GraphQLSchema create(String schema, String sourceName)
	{
		return create(new StringReader(schema), sourceName);
	}

	/**
	 * Create a schema from the given {@link Reader}.
	 *
	 * @param reader
	 *   reader to parse from
	 * @return
	 *   parsed schema
	 * @throws ModelValidationException
	 *   if schema is not valid
	 */
	public static GraphQLSchema create(Reader reader)
	{
		return create(reader, null);
	}

	/**
	 * Create a schema from the given {@link Reader}.
	 *
	 * @param reader
	 *   reader to parse from
	 * @param sourceName
	 *   name of source, commonly a filename
	 * @return
	 *   parsed schema
	 * @throws ModelValidationException
	 *   if schema is not valid
	 */
	@SuppressWarnings("rawtypes")
	public static GraphQLSchema create(Reader reader, String sourceName)
	{
		MutableList<TypeDef> types = Lists.mutable.empty();

		Document doc;
		try
		{
			Parser parser = new Parser();
			doc = parser.parseDocument(
				MultiSourceReader.newMultiSourceReader()
                	.reader(reader, sourceName)
                	.build()
			);
		}
		catch(InvalidSyntaxException e)
		{
			throw new ModelValidationException(
				"Unable to parse schema:",
				Lists.immutable.of(SYNTAX_ERROR.toMessage()
					.withLocation(toSourceLocation(e.getLocation()))
					.withArgument("message", e.getMessage())
					.build()
				)
			);
		}

		MutableList<ValidationMessage> messages = Lists.mutable.empty();

		for(Definition type : doc.getDefinitions())
		{
			if(type instanceof ObjectTypeDefinition o)
			{
				types.add(defineObject(o));
			}
			else if(type instanceof InterfaceTypeDefinition i)
			{
				types.add(defineInterface(i));
			}
			else if(type instanceof EnumTypeDefinition e)
			{
				types.add(defineEnum(e));
			}
			else if(type instanceof UnionTypeDefinition u)
			{
				types.add(defineUnion(u));
			}
			else if(type instanceof TypeDefinition<?> d)
			{
				messages.add(UNSUPPORTED_TYPE.toMessage()
					.withLocation(toSourceLocation(type))
					.withArgument("type", d.getName())
					.build()
				);
			}
			else
			{
				messages.add(SYNTAX_ERROR.toMessage()
					.withLocation(toSourceLocation(type))
					.withArgument("message", "Non-SDL content in schema")
					.build()
				);
			}
		}

		if(messages.anySatisfy(ValidationMessage.errorPredicate()))
		{
			// Schema has errors, throw a validation message
			throw new ModelValidationException(messages);
		}

		return new GraphQLSchema(types);
	}

	/**
	 * Create a {@link ObjectDef} from a {@link ObjectTypeDefinition}.
	 *
	 * @param def
	 *   definition to convert
	 * @return
	 *   converted {@link ObjectDef}
	 */
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

	/**
	 * Create a {@link InterfaceDef} from a {@link InterfaceTypeDefinition}.
	 *
	 * @param def
	 *   definition to convert
	 * @return
	 *   convert {@link InterfaceDef}
	 */
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

	/**
	 * Create a {@link EnumDef} from a {@link EnumTypeDefinition}.
	 *
	 * @param def
	 *   definition to convert
	 * @return
	 *   convert {@link EnumDef}
	 */
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

	/**
	 * Create a {@link UnionDef} from a {@link UnionTypeDefinition}.
	 *
	 * @param def
	 *   definition to convert
	 * @return
	 *   convert {@link UnionDef}
	 */
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

	/**
	 * Create an iterable with implemented types from an iterable of
	 * {@link Type}.
	 *
	 * @param types
	 *   iterable to convert
	 * @return
	 *   converted type
	 */
	@SuppressWarnings("rawtypes")
	private static Iterable<String> createImplements(
		Iterable<Type> types
	)
	{
		return Lists.immutable.ofAll(types)
			.collect(t -> ((TypeName) t).getName());
	}

	/**
	 * Create an iterable of {@link FieldDef} from an iterable of
	 * {@link FieldDefinition}.
	 *
	 * @param fields
	 *   fields to convert
	 * @return
	 *   converted iterable
	 */
	private static Iterable<FieldDef> createFields(
		Iterable<FieldDefinition> fields
	)
	{
		return Lists.immutable.ofAll(fields)
			.collect(GraphQLSchema::defineField);
	}

	/**
	 * Create a {@link FieldDef} from a {@link FieldDefinition}.
	 *
	 * @param def
	 *   field to convert
	 * @return
	 *   converted {@link FieldDef}
	 */
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

	/**
	 * Create an iterable of {@link ArgumentDef} from a {@link FieldDefinition}.
	 *
	 * @param def
	 *   field to get arguments for
	 * @return
	 *   iterable with {@link ArgumentDef}
	 */
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
					.withDefaultValue(arg.getDefaultValue() == null
						? null
						: toJavaValue(arg.getDefaultValue())
					)
					.build();
			});
	}

	/**
	 * Create an iterable of {@link DirectiveUse} for directives placed on
	 * a {@link DirectivesContainer}.
	 *
	 * @param def
	 *   container to get directives
	 * @return
	 *   iterable of {@link DirectiveUse}
	 */
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

	/**
	 * Create a {@link OutputTypeDef} based on a {@link Type}. Used for all
	 * output types in fields.
	 *
	 * @param type
	 *   type to convert
	 * @return
	 *   converted type
	 */
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

	/**
	 * Create a {@link InputTypeDef} based on a {@link Type}. Used for all
	 * input types in field arguments.
	 *
	 * @param type
	 *   type to convert
	 * @return
	 *   converted type
	 */
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

	/**
	 * Get a plain string description from a {@link DescribedNode}.
	 *
	 * @param node
	 *   node to get description for
	 * @return
	 *   description in plain text, or {@code null}
	 */
	private static String toDescription(DescribedNode<?> node)
	{
		return node.getDescription() == null
			? null
			: node.getDescription().getContent();
	}

	/**
	 * Perform a conversion to {@link SourceLocation} for a list.
	 *
	 * @param locations
	 *   locations to converter
	 * @return
	 *   source location
	 */
	private static SourceLocation toSourceLocation(
		List<graphql.language.SourceLocation> locations
	)
	{
		return locations.isEmpty()
			? SourceLocation.unknown()
			: toSourceLocation(locations.get(0));
	}

	/**
	 * Perform a conversion to {@link SourceLocation} for a {@link Node}.
	 *
	 * @param def
	 *   node to convert
	 * @return
	 *   source location
	 */
	private static SourceLocation toSourceLocation(Node<?> def)
	{
		return toSourceLocation(def.getSourceLocation());
	}

	/**
	 * Perform a conversion to {@link SourceLocation}.
	 *
	 * @param loc
	 *   location to convert
	 * @return
	 *   source location
	 */
	private static SourceLocation toSourceLocation(graphql.language.SourceLocation loc)
	{
		if(loc == null) return SourceLocation.unknown();

		return SourceLocation.line(loc.getSourceName(), loc.getLine(), loc.getColumn());
	}

	/**
	 * Convert a {@link Value} into a Java object.
	 *
	 * @param input
	 * @return
	 */
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
