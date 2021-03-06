package com.circumgraph.values;

import java.util.Optional;

import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.values.internal.StructuredValueImpl;

import org.eclipse.collections.api.map.MapIterable;

/**
 * Structured value that maps against {@link StructuredDef} which used for
 * {@link ObjectDef objects} and {@link InterfaceDef interfaces}.
 */
public interface StructuredValue
	extends Value
{
	/**
	 * Get the definition that represents this data.
	 *
	 * @return
	 */
	@Override
	StructuredDef getDefinition();

	/**
	 * Get all of the fields within this structured data.
	 *
	 * @return
	 */
	MapIterable<String, ? extends Value> getFields();

	/**
	 * Get a field with the given name.
	 *
	 * @param name
	 * @return
	 */
	Optional<? extends Value> getField(String name);

	/**
	 * Get a field with the given name and type.
	 *
	 * @param <V>
	 * @param name
	 * @param type
	 * @return
	 */
	<V extends Value> Optional<? extends V> getField(String name, Class<V> type);

	/**
	 * Start building a new instance of {@link StructuredValue}.
	 *
	 * @param definition
	 * @return
	 */
	static Builder create(StructuredDef definition)
	{
		return new StructuredValueImpl.BuilderImpl(definition);
	}

	/**
	 * Create a new instance using pre-built fields.
	 *
	 * @param definition
	 * @param fields
	 * @return
	 */
	static StructuredValue create(StructuredDef definition, MapIterable<String, ? extends Value> fields)
	{
		return new StructuredValueImpl(definition, fields);
	}

	/**
	 * Builder for instances of {@link StructuredValue}.
	 */
	interface Builder
	{
		/**
		 * Add a field to the value.
		 *
		 * @param name
		 * @param value
		 * @return
		 */
		Builder add(String name, Value value);

		/**
		 * Build the value.
		 *
		 * @return
		 */
		StructuredValue build();
	}
}
