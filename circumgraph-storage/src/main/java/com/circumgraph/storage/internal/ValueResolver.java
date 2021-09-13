package com.circumgraph.storage.internal;

import java.util.Map;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.scalars.ScalarConversionException;
import com.circumgraph.storage.scalars.Scalars;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;

/**
 * Utility that helps resolving a {@link Value} from a {@link TypeDef} and
 * a Java-representation.
 */
public class ValueResolver
{
	private static final ValidationMessageType INVALID_MODEL_DEF = ValidationMessageType.error()
		.withCode("value:invalid-type")
		.withMessage("Conversion of `{{type}}` is currently unsupported")
		.withArgument("type")
		.build();

	private static final ValidationMessageType INVALID_ENUM = ValidationMessageType.error()
		.withCode("value:invalid-enum-value")
		.withMessage("`{{value}}` does not exist for enum `{{type}}`")
		.withArgument("type")
		.withArgument("value")
		.build();

	private static final ValidationMessageType INVALID_LIST_VALUE = ValidationMessageType.error()
		.withCode("value:invalid-list-value")
		.withMessage("Can not convert into list, value was not iterable")
		.build();

	private static final ValidationMessageType INVALID_SCALAR_VALUE = ValidationMessageType.error()
		.withCode("value:invalid-scalar-value")
		.withMessage("Could not convert into `{{type}}`, conversion failed: {{message}}")
		.withArgument("type")
		.withArgument("message")
		.build();

	private static final ValidationMessageType INVALID_OBJECT_VALUE = ValidationMessageType.error()
		.withCode("value:invalid-object-value")
		.withMessage("Can not convert into object, value was not a map")
		.build();

	private static final ValidationMessageType INVALID_FIELD = ValidationMessageType.error()
		.withCode("value:invalid-field")
		.withMessage("Could not convert into `{{type}}`, field `{{field}}` does not exist")
		.withArgument("type")
		.withArgument("field")
		.build();

	private ValueResolver()
	{
	}

	public static Value resolve(
		ObjectLocation location,
		TypeDef modelDef,
		Object value
	)
	{
		if(value == null)
		{
			return null;
		}

		if(modelDef instanceof NonNullDef nonNullDef)
		{
			return resolve(location, nonNullDef.getType(), value);
		}
		else if(modelDef instanceof ScalarDef scalarDef)
		{
			var scalars = Scalars.instance();
			var scalar = scalars.get(scalarDef).orElseThrow();
			try
			{
				return SimpleValue.create(scalarDef, scalar.toJava(value));
			}
			catch(ScalarConversionException e)
			{
				throw new StorageValidationException(INVALID_SCALAR_VALUE.toMessage()
					.withLocation(location)
					.withArgument("type", scalarDef.getName())
					.withArgument("message", e.getMessage())
					.build()
				);
			}
		}
		else if(modelDef instanceof EnumDef enumDef)
		{
			var stringified = String.valueOf(value);
			for(var enumValueDef : enumDef.getValues())
			{
				if(stringified.equals(enumValueDef.getName()))
				{
					return SimpleValue.create(enumDef, stringified);
				}
			}

			throw new StorageValidationException(INVALID_ENUM.toMessage()
				.withLocation(location)
				.withArgument("type", enumDef.getName())
				.withArgument("value", stringified)
				.build()
			);
		}
		else if(modelDef instanceof ListDef listDef)
		{
			if(! (value instanceof Iterable<?> iterable))
			{
				throw new StorageValidationException(INVALID_LIST_VALUE.toMessage()
					.withLocation(location)
					.build()
				);
			}

			var itemTypeDef = listDef.getItemType();
			var converted = Lists.mutable.<Value>of();
			var i = 0;
			for(var item : iterable)
			{
				converted.add(resolve(location.forIndex(i), itemTypeDef, item));
				i++;
			}

			return ListValue.create(listDef, converted);
		}
		else if(modelDef instanceof ObjectDef objectDef)
		{
			MapIterable<String, Object> values;
			if(value instanceof MapIterable mapIterable)
			{
				values = mapIterable;
			}
			else if(value instanceof Map map)
			{
				values = Maps.immutable.ofAll(map);
			}
			else
			{
				throw new StorageValidationException(INVALID_OBJECT_VALUE.toMessage()
					.withLocation(location)
					.build()
				);
			}

			var builder = StructuredValue.create(objectDef);
			for(var e : values.keyValuesView())
			{
				var key = e.getOne();
				var fieldValue = e.getTwo();

				var field = objectDef.getField(key);
				if(field.isEmpty())
				{
					throw new StorageValidationException(INVALID_FIELD.toMessage()
						.withLocation(location.forField(key))
						.withArgument("type", modelDef.getName())
						.withArgument("key", value)
						.build()
					);
				}
				else
				{
					var f = field.get();
					builder = builder.add(key, resolve(location.forField(key), f.getType(), fieldValue));
				}
			}

			return builder.build();
		}

		throw new StorageValidationException(INVALID_MODEL_DEF.toMessage()
			.withArgument("type", modelDef.getName())
			.build()
		);
	}
}
