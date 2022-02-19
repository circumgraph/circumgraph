package com.circumgraph.storage;

import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.TypeDef;
import com.circumgraph.storage.internal.ValueResolver;

public interface Value
{
	/**
	 * Get the definition of this data.
	 *
	 * @return
	 */
	TypeDef getDefinition();


	/**
	 * Resolve a value of a specific type.
	 *
	 * @param def
	 * @param javaValue
	 * @throws StorageValidationException
	 *   if unable to resolve value
	 * @return
	 */
	static Value resolve(
		TypeDef def,
		Object javaValue
	)
	{
		return ValueResolver.resolve(ObjectLocation.root(), def, javaValue);
	}
}
