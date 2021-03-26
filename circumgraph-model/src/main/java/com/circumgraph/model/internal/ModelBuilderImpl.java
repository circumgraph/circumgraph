package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.HasSourceLocation;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.Model.Builder;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Multimaps;

/**
 * Implementation of {@link Model.Builder}.
 */
public class ModelBuilderImpl
	implements Model.Builder
{
	private final ImmutableSet<TypeDef> types;
	private final ImmutableSet<DirectiveValidator<?>> directiveValidators;

	public ModelBuilderImpl(
		ImmutableSet<TypeDef> types,
		ImmutableSet<DirectiveValidator<?>> directiveValidators
	)
	{
		this.types = types;
		this.directiveValidators = directiveValidators;
	}

	@Override
	public Builder addDirectiveValidator(DirectiveValidator<?> validator)
	{
		return new ModelBuilderImpl(
			types,
			directiveValidators.newWith(validator)
		);
	}

	@Override
	public Model.Builder addType(TypeDef type)
	{
		return new ModelBuilderImpl(
			types.newWith(type),
			directiveValidators
		);
	}

	@Override
	public Builder addSchema(Schema schema)
	{
		return new ModelBuilderImpl(
			types.newWithAll(schema.getTypes()),
			directiveValidators.newWithAll(schema.getDirectiveValidators())
		);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Model build()
	{
		var validationMessages = Lists.mutable.<ValidationMessage>empty();
		Consumer<ValidationMessage> validation = validationMessages::add;

		// Create the directive validator map
		MutableMultimap<String, DirectiveValidator<?>> directives = Multimaps.mutable.set.empty();
		for(DirectiveValidator<?> v : directiveValidators)
		{
			directives.put(v.getName(), v);
		}

		Consumer<HasDirectives> directiveValidator = def -> {
			for(DirectiveUse d : def.getDirectives())
			{
				boolean didValidate = false;

				for(DirectiveValidator validator : directives.get(d.getName()))
				{
					if(validator.getContextType().isAssignableFrom(def.getClass()))
					{
						validator.validate(
							(HasDirectives) def,
							d,
							validation
						);

						didValidate = true;
					}
				}

				if(! didValidate)
				{
					validation.accept(ValidationMessage.error()
						.withLocation(d.getSourceLocation())
						.withMessage("Directive @%s can not be used at this location", d.getName())
						.withCode("model:invalid-directive")
						.withArgument("directive", d.getName())
						.build()
					);
				}
			}
		};

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

		// Validate all the directives
		for(TypeDef type : typeMap)
		{
			if(type instanceof HasDirectives)
			{
				directiveValidator.accept((HasDirectives) type);
			}

			if(type instanceof StructuredDef)
			{
				StructuredDef structured = (StructuredDef) type;
				for(FieldDef field : structured.getDirectFields())
				{
					directiveValidator.accept(field);

					for(ArgumentDef arg : field.getArguments())
					{
						directiveValidator.accept(arg);
					}
				}
			}
		}

		// Raise an error if any validation error has been found
		Predicate<ValidationMessage> isError = m -> m.getLevel() == ValidationMessageLevel.ERROR;
		if(validationMessages.anySatisfy(isError))
		{
			throw new ModelException(
				"Invalid model, errors reported:\n"
				+ validationMessages.select(isError)
					.collect(msg -> "  * " + msg.getLocation() + ": " + msg.getMessage())
					.makeString("\n")
			);
		}

		// Prepare and create model
		ImmutableMap<String, TypeDef> types = typeMap.toImmutable();
		ModelDefs defs = types::get;

		for(TypeDef type : types)
		{
			HasPreparation.maybePrepare(type, defs);
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
		), Sets.immutable.empty());
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

		// Report not being able to merge
		validationCollector.accept(ValidationMessage.error()
			.withLocation(current instanceof HasSourceLocation ? ((HasSourceLocation) current).getSourceLocation() : null)
			.withMessage(
				"Could not merge: %s (at %s) has a different type than previously defined at %s",
				extension.getName(),
				toLocation(extension),
				toLocation(current)
			)
			.withCode("model:incompatible-types")
			.withArgument("name", current.getName())
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
			validationCollector.accept(ValidationMessage.error()
				.withLocation(f2.getSourceLocation())
				.withMessage(
					"Could not merge: %s in %s (at %s) has a different type than previously defined at %s",
					f2.getName(),
					type.getName(),
					toLocation(f2),
					toLocation(f1)
				)
				.withCode("model:incompatible-field-type")
				.withArgument("type", type.getName())
				.withArgument("field", f2.getName())
				.build()
			);

			return f1;
		}

		return FieldDef.create(f1.getName())
			.withSourceLocation(f1.getSourceLocation())
			.withDescription(pickFirstNonBlank(f1.getDescription(), f2.getDescription()))
			.withNullable(f1.isNullable())
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
			validationCollector.accept(ValidationMessage.error()
				.withLocation(a1.getSourceLocation())
				.withMessage(
					"Could not merge: %s in %s (at %s) has a different type than previously defined at %s",
					a1.getName(),
					type.getName(),
					toLocation(a2),
					toLocation(a1)
				)
				.withCode("model:incompatible-field-type")
				.withArgument("type", type.getName())
				.withArgument("field", field.getName())
				.withArgument("argument", a1.getName())
				.build()
			);

			return a1;
		}

		return ArgumentDef.create(a1.getName())
			.withSourceLocation(a1.getSourceLocation())
			.withType(a1.getType())
			.withDescription(pickFirstNonBlank(a1.getDescription(), a2.getDescription()))
			.withNullable(a1.isNullable())
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
			return ((HasSourceLocation) current).getSourceLocation().toString();
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
}
