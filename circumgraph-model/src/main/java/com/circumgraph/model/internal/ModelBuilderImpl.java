package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.HasSourceLocation;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.Model.Builder;
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
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.model.validation.ValidationMessageType;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
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
			.withMessage("Field `{{field}}` in `{{fieldType}}` is declared as `{{fieldType}}`, but type does not exist")
			.build();

	private static final ValidationMessageType FIELD_TYPE_OUTPUT =
		ValidationMessageType.error()
			.withCode("model:field:output-type-required")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withMessage("Field `{{field}}` in `{{fieldType}}` is declared as `{{fieldType}}`, but type is not an output type")
			.build();

	private static final ValidationMessageType ARGUMENT_TYPE_UNKNOWN =
		ValidationMessageType.error()
			.withCode("model:argument:type-unknown")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("argumentType")
			.withMessage("Argument `{{argument}}` of field `{{field}}` in `{{fieldType}}` is declared as `{{argumentType}}`, but type does not exist")
			.build();

	private static final ValidationMessageType ARGUMENT_TYPE_INPUT =
		ValidationMessageType.error()
			.withCode("model:argument:input-type-required")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("argumentType")
			.withMessage("Argument `{{argument}}` of field `{{field}}` in `{{fieldType}}` is declared as `{{argumentType}}`, but type is not an input type")
			.build();

	private static final ValidationMessageType INVALID_DIRECTIVE =
		ValidationMessageType.error()
			.withCode("model:invalid-directive")
			.withArgument("directive")
			.withMessage("Directive @{{directive}} can not be used at this location")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_TYPES =
		ValidationMessageType.error()
			.withCode("model:incompatible-type")
			.withArgument("type")
			.withArgument("originalLocation")
			.withMessage("Could not merge: {{type}} has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_FIELD_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-field-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("originalLocation")
			.withMessage("Could not merge: Field {{field}} in {{type}} has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_ARGUMENT_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-argument-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("argument")
			.withArgument("location")
			.withArgument("originalLocation")
			.withMessage("Could not merge: Argument {{argument}} in {{type}}.{{field}} has a different type than previously defined at {{originalLocation}}")
			.build();

	private static final ValidationMessageType INVALID_IMPLEMENTS =
		ValidationMessageType.error()
			.withCode("model:invalid-implements")
			.withArgument("type")
			.withArgument("implements")
			.withMessage("{{type}} can not implement {{implements}}, type is not an interface")
			.build();

	private static final ValidationMessageType INVALID_IMPLEMENTS_LOOP =
		ValidationMessageType.error()
			.withCode("model:invalid-implements-loop")
			.withArgument("type")
			.withArgument("implements")
			.withMessage("{{type}} can not implement {{implements}}, as {{implements}} already implements {{type}} creating a loop")
			.build();

	private static final ValidationMessageType INCOMPATIBLE_INTERFACE_FIELD_TYPE =
		ValidationMessageType.error()
			.withCode("model:incompatible-interface-field-type")
			.withArgument("type")
			.withArgument("field")
			.withArgument("fieldType")
			.withArgument("interface")
			.withArgument("interfaceFieldType")
			.withMessage("Field {{field}} in {{type}} is {{fieldType}} but was declared as {{interfaceFieldType}} in the implemented interface {{interface}}")
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Model build()
	{
		var validationMessages = Lists.mutable.<ValidationMessage>empty();
		Consumer<ValidationMessage> validation = validationMessages::add;

		// Go through and merge all types
		MutableMap<String, TypeDef> typeMap = Maps.mutable.empty();

		for(TypeDef def : types)
		{
			TypeDef current = typeMap.get(def.getName());
			if(current == null)
			{
				typeMap.put(def.getName(), def);
			}
			else
			{
				typeMap.put(def.getName(), merge(validation, current, def));
			}
		}

		// Create an instance of type finder to deal with lists
		TypeFinder typeFinder = name -> {
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
		};

		// Validate all of the types
		for(TypeDef type : typeMap)
		{
			validate(
				typeFinder,
				type,
				validation
			);
		}

		// Raise an error if any validation error has been found
		Predicate<ValidationMessage> isError = m -> m.getLevel() == ValidationMessageLevel.ERROR;
		if(validationMessages.anySatisfy(isError))
		{
			throw new ModelValidationException(validationMessages);
		}

		// Prepare and create model
		ImmutableMap<String, TypeDef> types = typeMap.toImmutable();
		ModelDefs defs = new ModelDefs()
		{
			@Override
			public RichIterable<? extends TypeDef> getAll()
			{
				return types;
			}

			public TypeDef maybeResolve(String name)
			{
				return types.get(name);
			}
		};

		for(TypeDef type : types)
		{
			HasPreparation.maybePrepare(type, defs);
		}

		// Process model after preparation
		process(validation);

		// Throw an error if processing fails
		if(validationMessages.anySatisfy(isError))
		{
			throw new ModelValidationException(validationMessages);
		}

		return new ModelImpl(types);
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
		if(current instanceof ObjectDef)
		{
			if(extension instanceof ObjectDef)
			{
				return mergeObject(validationCollector, (ObjectDef) current, (ObjectDef) extension);
			}
		}
		else if(current instanceof InterfaceDef)
		{
			if(extension instanceof InterfaceDef)
			{
				return mergeInterface(validationCollector, (InterfaceDef) current, (InterfaceDef) extension);
			}
		}
		else if(current instanceof UnionDef)
		{
			if(extension instanceof UnionDef)
			{
				return mergeUnion(validationCollector, (UnionDef) current, (UnionDef) extension);
			}
		}
		else if(current instanceof EnumDef)
		{
			if(extension instanceof EnumDef)
			{
				return mergeEnum(validationCollector, (EnumDef) current, (EnumDef) extension);
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
						.withArgument("type", name)
						.withArgument("implements", type.getName())
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
	private void process(Consumer<ValidationMessage> validation)
	{
		// Process directives
		for(DirectiveUseProcessor<?> v : directiveUseProcessors)
		{
			for(var type : types)
			{
				if(type instanceof HasDirectives hasDirectives)
				{
					processDirective(v, hasDirectives, validation);
				}

				if(type instanceof StructuredDef structured)
				{
					for(FieldDef def : structured.getDirectFields())
					{
						processDirective(v, def, validation);
					}
				}
			}
		}

		// Process all the types
		for(var type : types)
		{
			for(var processor : typeDefProcessors)
			{
				if(processor.getType().isAssignableFrom(type.getClass()))
				{
					((TypeDefProcessor) processor).process(type, validation);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processDirective(
		DirectiveUseProcessor<?> processor,
		HasDirectives hasDirectives,
		Consumer<ValidationMessage> validation
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

		((DirectiveUseProcessor) processor).process(hasDirectives, directive.get(), validation);
	}

	/**
	 * Helper to used to find types based on their name while also dealing
	 * with lists.
	 */
	interface TypeFinder
	{
		/**
		 * Get the declared type if available.
		 *
		 * @param name
		 * @return
		 */
		TypeDef get(String name);
	}
}
