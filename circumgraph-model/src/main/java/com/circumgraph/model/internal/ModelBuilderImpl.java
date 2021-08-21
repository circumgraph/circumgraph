package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.Buildable;
import com.circumgraph.model.Derivable;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.HasSourceLocation;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.Model.Builder;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ModelValidationException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

/**
 * Implementation of {@link Model.Builder}.
 */
public class ModelBuilderImpl
	implements Model.Builder
{
	private static final ValidationMessageType FIELD_TYPE_UNKNOWN =
		ValidationMessageType.error()
			.withCode("model:field:type-unknown")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withMessage("Field `{{field}}` in `{{type}}` is declared as `{{fieldType}}`, but type does not exist")
			.build();

	private static final ValidationMessageType FIELD_TYPE_OUTPUT =
		ValidationMessageType.error()
			.withCode("model:field:output-type-required")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withMessage("Field `{{field}}` in `{{type}}` is declared as `{{fieldType}}`, but type is not an output type")
			.build();

	private static final ValidationMessageType FIELD_TYPE_INPUT =
		ValidationMessageType.error()
			.withCode("model:field:input-type-required")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withMessage("Field `{{field}}` in `{{type}}` is declared as `{{fieldType}}`, but type is not an input type")
			.build();

	private static final ValidationMessageType ARGUMENT_TYPE_UNKNOWN =
		ValidationMessageType.error()
			.withCode("model:argument:type-unknown")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("argumentType")
			.withMessage("Argument `{{argument}}` of field `{{field}}` in `{{type}}` is declared as `{{argumentType}}`, but type does not exist")
			.build();

	private static final ValidationMessageType ARGUMENT_TYPE_INPUT =
		ValidationMessageType.error()
			.withCode("model:argument:input-type-required")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("argumentType")
			.withMessage("Argument `{{argument}}` of field `{{field}}` in `{{type}}` is declared as `{{argumentType}}`, but type is not an input type")
			.build();

	private static final ValidationMessageType INVALID_DIRECTIVE =
		ValidationMessageType.error()
			.withCode("model:invalid-directive")
			.withArgument("directive")
			.withMessage("Directive `@{{directive}}` can not be used at this location")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_TYPES =
		ValidationMessageType.error()
			.withCode("model:incompatible-type")
			.withArgument("type")
			.withArgument("originalLocation")
			.withMessage("Could not merge: `{{type}}` has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_FIELD_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-field-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("originalLocation")
			.withMessage("Could not merge: Field `{{field}}` in `{{type}}` has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_ARGUMENT_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-argument-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("location")
			.withArgument("originalLocation")
			.withMessage("Could not merge: Argument `{{argument}}` in `{{type}}.{{field}}` has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INVALID_IMPLEMENTS =
		ValidationMessageType.error()
			.withCode("model:invalid-implements")
			.withArgument("type")
			.withArgument("implements")
			.withMessage("`{{type}}` can not implement `{{implements}}`, type is not an interface")
			.build();

	private static final ValidationMessageType INVALID_IMPLEMENTS_LOOP =
		ValidationMessageType.error()
			.withCode("model:invalid-implements-loop")
			.withArgument("type")
			.withArgument("implements")
			.withMessage("`{{type}}` can not implement `{{implements}}`, as `{{implements}}` already implements `{{type}}` creating a loop")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_INTERFACE_FIELD_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-interface-field-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withArgument("interface")
			.withArgument("interfaceFieldType")
			.withMessage("Field `{{field}}` in `{{type}}` is `{{fieldType}}` but was declared as `{{interfaceFieldType}}` in the implemented interface `{{interface}}`")
			.build();

	private static final ValidationMessageType UNION_TYPE_UNKNOWN =
		ValidationMessageType.error()
			.withCode("model:union:unknown-type")
			.withArgument("type")
			.withArgument("subType")
			.withMessage("`{{subType}}` is part of union `{{type}}`, but type is not declared")
			.build();

	private static final ValidationMessageType UNION_TYPE_STRUCTURED =
		ValidationMessageType.error()
			.withCode("model:union:structured-type-required")
			.withArgument("type")
			.withArgument("subType")
			.withMessage("`{{subType}}` is part of union `{{type}}`, but is not a structured type, must be interface or object")
			.build();

	private final ImmutableSet<TypeDef> types;
	private final ImmutableList<DirectiveUseProcessor<?>> directiveUseProcessors;
	private final ImmutableList<TypeDefProcessor<?>> typeDefProcessors;

	private final MapIterable<String, DirectiveUseProcessor<?>> directiveMap;

	public ModelBuilderImpl(
		ImmutableSet<TypeDef> types,
		ImmutableList<DirectiveUseProcessor<?>> directiveUseProcessors,
		ImmutableList<TypeDefProcessor<?>> typeDefProcessors
	)
	{
		this.types = types;
		this.directiveUseProcessors = directiveUseProcessors;
		this.typeDefProcessors = typeDefProcessors;

		this.directiveMap = directiveUseProcessors.toMap(DirectiveUseProcessor::getName, d -> d);
	}

	@Override
	public Builder addSchema(Schema schema)
	{
		return new ModelBuilderImpl(
			types.newWithAll(schema.getTypes()),
			directiveUseProcessors.newWithAll(schema.getDirectiveUseProcessors()),
			typeDefProcessors.newWithAll(schema.getTypeDefProcessors())
		);
	}

	@Override
	public Model build()
	{
		var validationMessages = Lists.mutable.<ValidationMessage>empty();
		Consumer<ValidationMessage> validation = validationMessages::add;

		var encounter = new ProcessingEncounterImpl(
			validation
		);

		// Let the encounter receive the initial types
		for(var def : types)
		{
			encounter.addType(def);
		}

		while(true)
		{
			// Reset the encounter for this processing step
			encounter.reset();

			// Validate all of the types
			for(var type : encounter.typeMap)
			{
				validate(
					encounter,
					type,
					validation
				);
			}

			// Raise an error if any validation error has been found
			if(validationMessages.anySatisfy(ValidationMessage.errorPredicate()))
			{
				throw new ModelValidationException(validationMessages);
			}

			ImmutableMap<String, TypeDef> types = encounter.typeMap.toImmutable();
			ModelDefs defs = new ModelDefs()
			{
				@Override
				public RichIterable<? extends TypeDef> getAll()
				{
					return types;
				}

				@Override
				public TypeDef maybeResolve(String name)
				{
					return types.get(name);
				}
			};

			encounter.prepare(defs);

			// Process model after preparation
			process(encounter);

			// Throw an error if processing fails
			if(validationMessages.anySatisfy(ValidationMessage.errorPredicate()))
			{
				throw new ModelValidationException(validationMessages);
			}

			if(! encounter.modified)
			{
				return new ModelImpl(types);
			}
		}
	}

	public static ModelBuilderImpl create()
	{
		return new ModelBuilderImpl(Sets.immutable.of(
			ScalarDef.BOOLEAN,
			ScalarDef.FLOAT,
			ScalarDef.INT,
			ScalarDef.STRING,
			ScalarDef.ID
		), Lists.immutable.empty(), Lists.immutable.empty());
	}

	private TypeDef merge(
		Consumer<ValidationMessage> validationCollector,
		TypeDef current,
		TypeDef extension
	)
	{
		if(current.equals(extension))
		{
			return current;
		}

		if(current instanceof ObjectDef d1)
		{
			if(extension instanceof ObjectDef d2)
			{
				return mergeObject(validationCollector, d1, d2);
			}
		}
		else if(current instanceof InterfaceDef d1)
		{
			if(extension instanceof InterfaceDef d2)
			{
				return mergeInterface(validationCollector, d1, d2);
			}
		}
		else if(current instanceof UnionDef d1)
		{
			if(extension instanceof UnionDef d2)
			{
				return mergeUnion(validationCollector, d1, d2);
			}
		}
		else if(current instanceof EnumDef d1)
		{
			if(extension instanceof EnumDef d2)
			{
				return mergeEnum(validationCollector, d1, d2);
			}
		}
		else if(current instanceof InputObjectDef d1)
		{
			if(extension instanceof InputObjectDef d2)
			{
				return mergeInputObject(validationCollector, d1, d2);
			}
		}

		// Report not being able to merge
		validationCollector.accept(INCOMPATIBLE_TYPES.toMessage()
			.withLocation(
				current instanceof HasSourceLocation
					? ((HasSourceLocation) current).getSourceLocation()
					: SourceLocation.unknown()
			)
			.withArgument("type", current.getName())
			.withArgument("originalLocation", toLocation(current))
			.build()
		);

		return current;
	}

	/**
	 * Merge two object definitions.
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	private ObjectDef mergeObject(
		Consumer<ValidationMessage> validationCollector,
		ObjectDef d1,
		ObjectDef d2
	)
	{
		return ObjectDef.create(d1.getName())
			.withSourceLocation(d1.getSourceLocation())
			.withDescription(pickFirstNonBlank(d1.getDescription(), d2.getDescription()))
			.addImplementsAll(mergeImplements(validationCollector, d1, d2))
			.addDirectives(mergeDirectives(validationCollector, d1, d2))
			.addFields(mergeFields(validationCollector, d1, d1.getDirectFields(), d2.getDirectFields()))
			.withAllMetadata(mergeMetadata(validationCollector, d1, d2))
			.build();
	}

	private InterfaceDef mergeInterface(
		Consumer<ValidationMessage> validationCollector,
		InterfaceDef d1,
		InterfaceDef d2
	)
	{
		return InterfaceDef.create(d1.getName())
			.withSourceLocation(d1.getSourceLocation())
			.withDescription(pickFirstNonBlank(d1.getDescription(), d2.getDescription()))
			.addImplementsAll(mergeImplements(validationCollector, d1, d2))
			.addDirectives(mergeDirectives(validationCollector, d1, d2))
			.addFields(mergeFields(validationCollector, d1, d1.getDirectFields(), d2.getDirectFields()))
			.withAllMetadata(mergeMetadata(validationCollector, d1, d2))
			.build();
	}

	private ListIterable<String> mergeImplements(
		Consumer<ValidationMessage> validationCollector,
		StructuredDef t1,
		StructuredDef t2
	)
	{
		MutableSet<String> result = Sets.mutable.ofAll(t1.getImplementsNames());
		result.addAllIterable(t2.getImplementsNames());
		return result.toList();
	}

	private ListIterable<FieldDef> mergeFields(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		RichIterable<FieldDef> f1,
		RichIterable<FieldDef> f2
	)
	{
		MutableMap<String, FieldDef> fields = Maps.mutable.empty();
		Procedure<FieldDef> p = field -> {
			if(fields.containsKey(field.getName()))
			{
				fields.put(field.getName(), mergeField(
					validationCollector,
					type,
					fields.get(field.getName()),
					field
				));
			}
			else
			{
				fields.put(field.getName(), field);
			}
		};

		f1.forEach(p);
		f2.forEach(p);

		return fields.toList();
	}

	private FieldDef mergeField(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		FieldDef f1,
		FieldDef f2
	)
	{
		if(! Objects.equals(f1.getTypeName(), f2.getTypeName()))
		{
			validationCollector.accept(INCOMPATIBLE_FIELD_TYPE.toMessage()
				.withLocation(f2.getSourceLocation())
				.withArgument("type", type.getName())
				.withArgument("field", f2.getName())
				.withArgument("originalLocation", toLocation(f1))
				.build()
			);

			return f1;
		}

		return FieldDef.create(f1.getName())
			.withSourceLocation(f1.getSourceLocation())
			.withDescription(pickFirstNonBlank(f1.getDescription(), f2.getDescription()))
			.withType(f1.getType())
			.addArguments(mergeArguments(validationCollector, type, f1, f1.getArguments(), f2.getArguments()))
			.addDirectives(mergeDirectives(validationCollector, f1, f2))
			.withAllMetadata(mergeMetadata(validationCollector, f1, f2))
			.build();
	}

	private ListIterable<ArgumentDef> mergeArguments(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		FieldDef field,
		RichIterable<ArgumentDef> a1,
		RichIterable<ArgumentDef> a2
	)
	{
		MutableMap<String, ArgumentDef> arguments = Maps.mutable.empty();
		Procedure<ArgumentDef> p = arg -> {
			if(arguments.containsKey(arg.getName()))
			{
				arguments.put(arg.getName(), mergeArgument(
					validationCollector,
					type,
					field,
					arguments.get(arg.getName()),
					arg
				));
			}
			else
			{
				arguments.put(arg.getName(), arg);
			}
		};

		a1.forEach(p);
		a2.forEach(p);

		return arguments.toList();
	}

	private ArgumentDef mergeArgument(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		FieldDef field,
		ArgumentDef a1,
		ArgumentDef a2
	)
	{
		if(! Objects.equals(a1.getTypeName(), a2.getTypeName()))
		{
			validationCollector.accept(INCOMPATIBLE_ARGUMENT_TYPE.toMessage()
				.withLocation(a2.getSourceLocation())
				.withArgument("type", type.getName())
				.withArgument("field", field.getName())
				.withArgument("argument", a1.getName())
				.withArgument("originalLocation", toLocation(a1))
				.build()
			);

			return a1;
		}

		return ArgumentDef.create(a1.getName())
			.withSourceLocation(a1.getSourceLocation())
			.withType(a1.getType())
			.withDescription(pickFirstNonBlank(a1.getDescription(), a2.getDescription()))
			.addDirectives(mergeDirectives(validationCollector, a1, a2))
			.build();
	}

	/**
	 * Merge two {@link InputObjectDef} instances.
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	private InputObjectDef mergeInputObject(
		Consumer<ValidationMessage> validationCollector,
		InputObjectDef d1,
		InputObjectDef d2
	)
	{
		return InputObjectDef.create(d1.getName())
			.withSourceLocation(d1.getSourceLocation())
			.withDescription(pickFirstNonBlank(d1.getDescription(), d2.getDescription()))
			.addDirectives(mergeDirectives(validationCollector, d1, d2))
			.addFields(mergeInputFields(validationCollector, d1, d1.getFields(), d2.getFields()))
			.withAllMetadata(mergeMetadata(validationCollector, d1, d2))
			.build();
	}

	/**
	 * Merge two lists of {@link InputFieldDef} together.
	 *
	 * @param validationCollector
	 * @param type
	 * @param f1
	 * @param f2
	 * @return
	 */
	private ListIterable<InputFieldDef> mergeInputFields(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		RichIterable<InputFieldDef> f1,
		RichIterable<InputFieldDef> f2
	)
	{
		MutableMap<String, InputFieldDef> fields = Maps.mutable.empty();
		Procedure<InputFieldDef> p = field -> {
			if(fields.containsKey(field.getName()))
			{
				fields.put(field.getName(), mergeInputField(
					validationCollector,
					type,
					fields.get(field.getName()),
					field
				));
			}
			else
			{
				fields.put(field.getName(), field);
			}
		};

		f1.forEach(p);
		f2.forEach(p);

		return fields.toList();
	}

	/**
	 * Merge two instances of {@link InputFieldDef} together.
	 *
	 * @param validationCollector
	 * @param type
	 * @param f1
	 * @param f2
	 * @return
	 */
	private InputFieldDef mergeInputField(
		Consumer<ValidationMessage> validationCollector,
		TypeDef type,
		InputFieldDef f1,
		InputFieldDef f2
	)
	{
		if(! Objects.equals(f1.getTypeName(), f2.getTypeName()))
		{
			validationCollector.accept(INCOMPATIBLE_FIELD_TYPE.toMessage()
				.withLocation(f2.getSourceLocation())
				.withArgument("type", type.getName())
				.withArgument("field", f2.getName())
				.withArgument("originalLocation", toLocation(f1))
				.build()
			);

			return f1;
		}

		return InputFieldDef.create(f1.getName())
			.withSourceLocation(f1.getSourceLocation())
			.withDescription(pickFirstNonBlank(f1.getDescription(), f2.getDescription()))
			.withType(f1.getType())
			.addDirectives(mergeDirectives(validationCollector, f1, f2))
			.withAllMetadata(mergeMetadata(validationCollector, f1, f2))
			.build();
	}

	private ListIterable<DirectiveUse> mergeDirectives(
		Consumer<ValidationMessage> validationCollector,
		HasDirectives d1,
		HasDirectives d2
	)
	{
		MutableMap<String, DirectiveUse> directives = Maps.mutable.empty();
		Procedure<DirectiveUse> p = d -> {
			if(directives.containsKey(d.getName()))
			{
				directives.put(d.getName(), mergeDirective(
					validationCollector,
					directives.get(d.getName()),
					d
				));
			}
			else
			{
				directives.put(d.getName(), d);
			}
		};

		d1.getDirectives().forEach(p);
		d2.getDirectives().forEach(p);

		return directives.toList();
	}

	private DirectiveUse mergeDirective(
		Consumer<ValidationMessage> validationCollector,
		DirectiveUse d1,
		DirectiveUse d2
	)
	{
		// TODO: Logic for merging the directives
		return d1;
	}

	private UnionDef mergeUnion(
		Consumer<ValidationMessage> validationCollector,
		UnionDef d1,
		UnionDef d2
	)
	{
		MutableSet<String> result = Sets.mutable.ofAll(d1.getTypeNames());
		result.addAllIterable(d2.getTypeNames());

		return UnionDef.create(d1.getName())
			.withSourceLocation(d1.getSourceLocation())
			.withDescription(pickFirstNonBlank(d1.getDescription(), d2.getDescription()))
			.addTypes(result.collect(TypeRef::create))
			.addDirectives(mergeDirectives(validationCollector, d1, d2))
			.build();
	}

	private EnumDef mergeEnum(
		Consumer<ValidationMessage> validationCollector,
		EnumDef d1,
		EnumDef d2
	)
	{
		MutableMap<String, EnumValueDef> arguments = Maps.mutable.empty();
		Procedure<EnumValueDef> p = v1 -> {
			if(arguments.containsKey(v1.getName()))
			{
				var v2 = arguments.get(v1.getName());

				var mergedValue = EnumValueDef.create(v1.getName())
					.withSourceLocation(v1.getSourceLocation())
					.addDirectives(mergeDirectives(validationCollector, v1, v2))
					.build();

				arguments.put(v1.getName(), mergedValue);
			}
			else
			{
				arguments.put(v1.getName(), v1);
			}
		};

		d1.getValues().forEach(p);
		d2.getValues().forEach(p);

		return EnumDef.create(d1.getName())
			.withSourceLocation(d1.getSourceLocation())
			.withDescription(pickFirstNonBlank(d1.getDescription(), d2.getDescription()))
			.addValues(arguments)
			.addDirectives(mergeDirectives(validationCollector, d1, d2))
			.build();
	}

	private RichIterable<MetadataDef> mergeMetadata(
		Consumer<ValidationMessage> validationCollector,
		HasMetadata d1,
		HasMetadata d2
	)
	{
		var result = Lists.mutable.<MetadataDef>empty();
		result.addAllIterable(d1.getDefinedMetadata());
		result.addAllIterable(d2.getDefinedMetadata());
		return result;
	}

	/**
	 * Get a description of where the given type has been defined.
	 *
	 * @param current
	 * @return
	 */
	private String toLocation(Object current)
	{
		if(current instanceof HasSourceLocation)
		{
			return ((HasSourceLocation) current).getSourceLocation().describe();
		}
		else
		{
			return "Unknown Location";
		}
	}

	private static String pickFirstNonBlank(Optional<String> a, Optional<String> b)
	{
		return a.isEmpty() || a.get().isBlank() ? b.orElse(null) : a.get();
	}

	private void validate(
		TypeFinder types,
		TypeDef type,
		Consumer<ValidationMessage> validationCollector
	)
	{
		// TODO: Schema specific validation

		// Validate that the directives will be processed
		if(type instanceof HasDirectives hasDirectives)
		{
			validateDirectives(hasDirectives, validationCollector);
		}

		if(type instanceof StructuredDef structured)
		{
			validateImplements(types, structured, validationCollector);

			for(var field : structured.getDirectFields())
			{
				validateField(types, structured, field, validationCollector);
			}
		}
		else if(type instanceof InputObjectDef i)
		{
			for(var field : i.getFields())
			{
				validateInputField(types, i, field, validationCollector);
			}
		}
		else if(type instanceof UnionDef u)
		{
			validateUnion(types, u, validationCollector);
		}
	}

	/**
	 * Validate a {@link FieldDef}.
	 *
	 * @param types
	 * @param structured
	 * @param field
	 * @param validationCollector
	 */
	private void validateField(
		TypeFinder types,
		StructuredDef structured,
		FieldDef field,
		Consumer<ValidationMessage> validationCollector
	)
	{
		validateDirectives(field, validationCollector);

		// Validate the type
		var fieldType = field.getType();
		if(fieldType instanceof NonNullDef.Output n)
		{
			fieldType = n.getType();
		}

		if(fieldType instanceof ListDef.Output l)
		{
			fieldType = l.getItemType();

			if(fieldType instanceof NonNullDef.Output n)
			{
				fieldType = n.getType();
			}
		}

		var resolvedType = types.get(fieldType.getName());
		if(resolvedType == null)
		{
			// This type does not exist, report error
			validationCollector.accept(FIELD_TYPE_UNKNOWN.toMessage()
				.withLocation(field)
				.withArgument("type", structured.getName())
				.withArgument("field", field.getName())
				.withArgument("fieldType", fieldType.getName())
				.build()
			);
		}
		else if(! (resolvedType instanceof OutputTypeDef))
		{
			// The type of fields must be output types
			validationCollector.accept(FIELD_TYPE_OUTPUT.toMessage()
				.withLocation(field)
				.withArgument("type", structured.getName())
				.withArgument("field", field.getName())
				.withArgument("fieldType", fieldType.getName())
				.build()
			);
		}

		// Check arguments
		for(var arg : field.getArguments())
		{
			validateArgument(types, structured, field, arg, validationCollector);
		}
	}

	/**
	 * Validate a {@link ArgumentDef}.
	 *
	 * @param types
	 * @param structured
	 * @param field
	 * @param argument
	 * @param validationCollector
	 */
	private void validateArgument(
		TypeFinder types,
		StructuredDef structured,
		FieldDef field,
		ArgumentDef argument,
		Consumer<ValidationMessage> validationCollector
	)
	{
		validateDirectives(argument, validationCollector);

		// Validate the type
		var fieldType = argument.getType();
		if(fieldType instanceof NonNullDef.Input n)
		{
			fieldType = n.getType();
		}

		if(fieldType instanceof ListDef.Input l)
		{
			fieldType = l.getItemType();

			if(fieldType instanceof NonNullDef.Input n)
			{
				fieldType = n.getType();
			}
		}

		var resolvedType = types.get(fieldType.getName());
		if(resolvedType == null)
		{
			// This type does not exist, report error
			validationCollector.accept(ARGUMENT_TYPE_UNKNOWN.toMessage()
				.withLocation(argument)
				.withArgument("type", structured.getName())
				.withArgument("field", field.getName())
				.withArgument("argument", argument.getName())
				.withArgument("argumentType", fieldType.getName())
				.build()
			);
		}
		else if(! (resolvedType instanceof InputTypeDef))
		{
			// The type of arguments must be input types
			validationCollector.accept(ARGUMENT_TYPE_INPUT.toMessage()
				.withLocation(field)
				.withArgument("type", structured.getName())
				.withArgument("field", field.getName())
				.withArgument("argument", argument.getName())
				.withArgument("argumentType", fieldType.getName())
				.build()
			);
		}
	}

	/**
	 * Validate a {@link InputFieldDef}.
	 *
	 * @param types
	 * @param inputObject
	 * @param field
	 * @param validationCollector
	 */
	private void validateInputField(
		TypeFinder types,
		InputObjectDef inputObject,
		InputFieldDef field,
		Consumer<ValidationMessage> validationCollector
	)
	{
		validateDirectives(field, validationCollector);

		// Validate the type
		var fieldType = field.getType();
		if(fieldType instanceof NonNullDef.Input n)
		{
			fieldType = n.getType();
		}

		if(fieldType instanceof ListDef.Input l)
		{
			fieldType = l.getItemType();

			if(fieldType instanceof NonNullDef.Input n)
			{
				fieldType = n.getType();
			}
		}

		var resolvedType = types.get(fieldType.getName());
		if(resolvedType == null)
		{
			// This type does not exist, report error
			validationCollector.accept(FIELD_TYPE_UNKNOWN.toMessage()
				.withLocation(field)
				.withArgument("type", inputObject.getName())
				.withArgument("field", field.getName())
				.withArgument("fieldType", fieldType.getName())
				.build()
			);
		}
		else if(! (resolvedType instanceof InputTypeDef))
		{
			// The type of fields must be output types
			validationCollector.accept(FIELD_TYPE_INPUT.toMessage()
				.withLocation(field)
				.withArgument("type", inputObject.getName())
				.withArgument("field", field.getName())
				.withArgument("fieldType", fieldType.getName())
				.build()
			);
		}
	}

	private void validateUnion(
		TypeFinder types,
		UnionDef union,
		Consumer<ValidationMessage> validationCollector
	)
	{
		for(var type : union.getTypes())
		{
			// Validate the type
			TypeDef foundType = type;
			if(type instanceof TypeRef r)
			{
				// Resolve the type
				foundType = types.get(type.getName());
			}

			if(foundType == null)
			{
				validationCollector.accept(UNION_TYPE_UNKNOWN.toMessage()
					.withLocation(union)
					.withArgument("type", union.getName())
					.withArgument("subType", type.getName())
					.build()
				);
			}
			else if(! (foundType instanceof StructuredDef))
			{
				validationCollector.accept(UNION_TYPE_STRUCTURED.toMessage()
					.withLocation(union)
					.withArgument("type", union.getName())
					.withArgument("subType", type.getName())
					.build()
				);
			}
		}
	}

	/**
	 * Validate that all of the directives places on something will be
	 * processed.
	 *
	 * @param hasDirectives
	 */
	private void validateDirectives(
		HasDirectives hasDirectives,
		Consumer<ValidationMessage> validationCollector
	)
	{
		for(var directive : hasDirectives.getDirectives())
		{
			var processor = directiveMap.get(directive.getName());
			if(processor == null || ! processor.getContextType().isAssignableFrom(hasDirectives.getClass()))
			{
				validationCollector.accept(INVALID_DIRECTIVE.toMessage()
					.withLocation(
						hasDirectives instanceof HasSourceLocation source
							? source.getSourceLocation()
							: SourceLocation.unknown()
					)
					.withArgument("directive", directive.getName())
					.build()
				);
			}
		}
	}

	private void validateImplements(
		TypeFinder types,
		StructuredDef type,
		Consumer<ValidationMessage> validationCollector
	)
	{
		var fields = type.getDirectFields()
			.toMap(FieldDef::getName, f -> f);

		for(String name : type.getImplementsNames())
		{
			var asType = types.get(name);
			if(! (asType instanceof InterfaceDef))
			{
				validationCollector.accept(INVALID_IMPLEMENTS.toMessage()
					.withLocation(type)
					.withArgument("type", type.getName())
					.withArgument("implements", name)
					.build()
				);

				continue;
			}
			else
			{
				// Check that the fields of this interface are compatible with ours
				var interfaceDef = (InterfaceDef) asType;
				for(var otherField : interfaceDef.getDirectFields())
				{
					var ownField = fields.get(otherField.getName());
					if(ownField == null)
					{
						/*
						 * In GraphQL schemas you have to redefine fields,
						 * to make things a bit nicer we skip this requirement
						 * so this case is valid.
						 */
						continue;
					}

					if(! checkIfCompatible(types, ownField.getTypeName(), otherField.getTypeName()))
					{
						// Type of fields are not compatible
						validationCollector.accept(INCOMPATIBLE_INTERFACE_FIELD_TYPE.toMessage()
							.withLocation(ownField)
							.withArgument("type", type.getName())
							.withArgument("field", ownField.getName())
							.withArgument("fieldType", ownField.getTypeName())
							.withArgument("interface", interfaceDef.getName())
							.withArgument("interfaceFieldType", otherField.getTypeName())
							.build()
						);
					}
				}
			}
		}

		/*
		 * Recurse down into types making sure that there are no loops such
		 * as interface A implements B and B implements A.
		 */
		var stack = type.getImplementsNames().toStack();
		var checked = Sets.mutable.<String>empty();
		while(! stack.isEmpty())
		{
			var implementedName = stack.pop();
			var implementedAsType = types.get(implementedName);
			if(! (implementedAsType instanceof InterfaceDef))
			{
				// Correct type is validated earlier
				continue;
			}

			for(String name : ((InterfaceDef) implementedAsType).getImplementsNames())
			{
				if(name.equals(type.getName()))
				{
					// This interface implements us, but we also implement it
					validationCollector.accept(INVALID_IMPLEMENTS_LOOP.toMessage()
						.withLocation((InterfaceDef) implementedAsType)
						.withArgument("type", implementedName)
						.withArgument("implements", name)
						.build()
					);
				}

				if(checked.add(name))
				{
					// Descend into the type to validate
					stack.push(name);
				}
			}
		}
	}

	/**
	 * Check if the two given types are compatible. Types are compatible if:
	 *
	 * 1. They are the same type
	 * 2. A implements B either directly or indirectly
	 * 3. A and B are lists with types that are compatible
	 *
	 * @param types
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean checkIfCompatible(
		TypeFinder types,
		String a,
		String b
	)
	{
		// Case 1: Same type
		if(a.equals(b)) return true;

		var typeA = types.get(a);
		var typeB = types.get(b);

		// Sanity check: Types must exist
		if(typeA == null || typeB == null) return false;

		// Case 2: A implements B
		if(typeA instanceof StructuredDef && typeB instanceof StructuredDef)
		{
			var structuredA = (StructuredDef) typeA;

			var checked = Sets.mutable.ofAll(structuredA.getImplementsNames());
			var stack = structuredA.getImplementsNames().toStack();

			while(! stack.isEmpty())
			{
				var name = stack.pop();
				if(name.equals(b))
				{
					return true;
				}

				var interfaceDef = types.get(name);
				if(interfaceDef instanceof InterfaceDef)
				{
					for(var interfaceName : ((InterfaceDef) interfaceDef).getImplementsNames())
					{
						if(checked.add(interfaceName))
						{
							stack.push(interfaceName);
						}
					}
				}
			}
		}

		// Case 3: A and B are lists
		if(typeA instanceof ListDef && typeB instanceof ListDef)
		{

			return checkIfCompatible(
				types,
				((ListDef) typeA).getItemTypeName(),
				((ListDef) typeB).getItemTypeName()
			);
		}

		// Default to not being compatible
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void process(ProcessingEncounterImpl encounter)
	{
		// Process directives
		for(DirectiveUseProcessor<?> v : directiveUseProcessors)
		{
			for(var type : encounter.types())
			{
				if(type instanceof HasDirectives hasDirectives)
				{
					processDirective(v, hasDirectives, encounter);
				}

				if(type instanceof StructuredDef structuredDef)
				{
					for(var fieldDef : structuredDef.getDirectFields())
					{
						processDirective(v, fieldDef, encounter);

						for(var arg : fieldDef.getArguments())
						{
							processDirective(v, arg, encounter);
						}
					}
				}
				else if(type instanceof InputObjectDef inputObjectDef)
				{
					for(var field : inputObjectDef.getFields())
					{
						processDirective(v, field, encounter);
					}
				}
			}

			if(encounter.modified)
			{
				// If the schema was modified we need to reprocess
				return;
			}
		}

		// Process all the types
		for(var processor : typeDefProcessors)
		{
			for(var type : encounter.types())
			{
				if(processor.getType().isAssignableFrom(type.getClass()))
				{
					((TypeDefProcessor) processor).process(encounter, type);
				}
			}

			if(encounter.modified)
			{
				// If the schema was modified we need to reprocess
				return;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processDirective(
		DirectiveUseProcessor<?> processor,
		HasDirectives hasDirectives,
		ProcessingEncounter encounter
	)
	{
		if(! processor.getContextType().isAssignableFrom(hasDirectives.getClass()))
		{
			return;
		}

		var directive = hasDirectives.getDirective(processor.getName());
		if(directive.isEmpty())
		{
			return;
		}

		((DirectiveUseProcessor) processor).process(encounter, hasDirectives, directive.get());
	}

	/**
	 * Helper to used to find types based on their name while also dealing
	 * with lists.
	 */
	private interface TypeFinder
	{
		/**
		 * Get the declared type if available.
		 *
		 * @param name
		 * @return
		 */
		TypeDef get(String name);
	}

	private class ProcessingEncounterImpl
		implements ProcessingEncounter, TypeFinder
	{
		private final Consumer<ValidationMessage> validationCollector;
		private final MutableMap<String, TypeDef> typeMap;

		/**
		 * Flag that is set if the processing modifies types in any way.
		 */
		private boolean modified;
		private RichIterable<TypeDef> types;

		public ProcessingEncounterImpl(
			Consumer<ValidationMessage> validationCollector
		)
		{
			this.validationCollector = validationCollector;
			this.typeMap = Maps.mutable.empty();
		}

		public void prepare(ModelDefs defs)
		{
			for(TypeDef type : types)
			{
				HasPreparation.maybePrepare(type, defs);
			}
		}

		public RichIterable<TypeDef> types()
		{
			return types;
		}

		public void reset()
		{
			modified = false;
			this.types = Lists.immutable.ofAll(typeMap);
		}

		@Override
		public void report(ValidationMessage message)
		{
			this.validationCollector.accept(message);
		}

		@Override
		public TypeDef get(String name)
		{
			if(name.isEmpty())
			{
				return null;
			}
			else if(name.charAt(0) == '[')
			{
				var type = typeMap.get(name.substring(1, name.length() - 1));
				if(type == null) return null;

				return type instanceof OutputTypeDef
					? ListDef.output((OutputTypeDef) type)
					: ListDef.input((InputTypeDef) type);
			}
			else
			{
				return typeMap.get(name);
			}
		}

		@Override
		public void addType(TypeDef def)
		{
			var current = typeMap.get(def.getName());
			TypeDef updated;
			if(current == null)
			{
				updated = def;
			}
			else
			{
				updated = merge(validationCollector, current, def);
			}

			if(! updated.equals(current))
			{
				typeMap.put(def.getName(), updated);
				modified = true;

				addReferencedTypes(updated);
			}
		}

		@Override
		public void replaceType(TypeDef type)
		{
			var current = typeMap.get(type.getName());
			if(! type.equals(current))
			{
				typeMap.put(type.getName(), type);
				modified = true;

				addReferencedTypes(type);
			}
		}

		/**
		 * Inspect a type and add any types it references.
		 *
		 * @param def
		 */
		private void addReferencedTypes(TypeDef def)
		{
			if(def instanceof StructuredDef s)
			{
				for(var field : s.getDirectFields())
				{
					unroll(field.getType()).ifPresent(this::addType);

					for(var arg : field.getArguments())
					{
						unroll(arg.getType()).ifPresent(this::addType);
					}
				}
			}
			else if(def instanceof UnionDef u)
			{
				for(var type : u.getTypes())
				{
					unroll(type).ifPresent(this::addType);
				}
			}
			else if(def instanceof InputObjectDef i)
			{
				for(var field : i.getFields())
				{
					unroll(field.getType()).ifPresent(this::addType);
				}
			}
		}

		/**
		 * Unroll a type until it's not a non-null, a list or a reference.
		 *
		 * @param d
		 * @return
		 */
		private Optional<TypeDef> unroll(TypeDef d)
		{
			if(d instanceof NonNullDef n)
			{
				return unroll(n.getType());
			}
			else if(d instanceof ListDef l)
			{
				return unroll(l.getItemType());
			}

			return d instanceof TypeRef ? Optional.empty() : Optional.ofNullable(d);
		}

		@Override
		public <B extends Buildable<?>, D extends Derivable<B>> void edit(
			D instance,
			Function<B, B> editor
		)
		{
			B builder = instance.derive();
			D edited = (D) editor.apply(builder).build();

			if(edited instanceof TypeDef def)
			{
				replaceType(def);
			}
			else if(edited instanceof FieldDef def)
			{
				var parent = (StructuredDef) typeMap.get(
					((FieldDef) instance).getDeclaringType().getName()
				);

				replaceType(parent.derive()
					.addField(def)
					.build()
				);
			}
			else if(edited instanceof ArgumentDef argDef)
			{
				edit(((ArgumentDef) instance).getDeclaringField(), b -> b.addArgument(argDef));
			}
			else if(edited instanceof InputFieldDef def)
			{
				var parent = (InputObjectDef) typeMap.get(
					((InputFieldDef) instance).getDeclaringType().getName()
				);

				replaceType(parent.derive()
					.addField(def)
					.build()
				);
			}
			else
			{
				throw new ModelException("Unknown type of object, can not edit: " + instance);
			}
		}
	}
}
