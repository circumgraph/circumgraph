package com.circumgraph.model.internal;

import com.circumgraph.model.ModelException;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;

import org.eclipse.collections.api.RichIterable;

/**
 * Internal provider of types.
 */
public interface ModelDefs
{
	/**
	 * Get all of the types.
	 *
	 * @return
	 */
	RichIterable<? extends TypeDef> getAll();

	/**
	 * Resolve a type based on its name.
	 *
	 * @param name
	 *   name of type
	 * @return
	 *   type or {@code null} if type can not be found
	 */
	TypeDef maybeResolve(String name);

	/**
	 * Get a type based on its name. Will throw {@link ModelException} if the
	 * type does not exist.
	 *
	 * @param name
	 *   name of type
	 * @return
	 *   type
	 */
	default TypeDef getType(String name)
	{
		TypeDef type = maybeResolve(name);
		if(type == null)
		{
			throw new ModelException("Can not find type with id " + name);
		}

		return type;
	}

	/**
	 * Get a type based on its name. Will throw {@link ModelException} if the
	 * type does not exist or if its of the wrong type.
	 *
	 * @param <T>
	 *   type
	 * @param name
	 *   name of type to get
	 * @param expectedClass
	 *   the expected class of the type
	 * @return
	 */
	default <T extends TypeDef> T getType(String name, Class<T> expectedClass)
	{
		TypeDef instance = getType(name);
		if(! expectedClass.isAssignableFrom(instance.getClass()))
		{
			throw new ModelException("Expected type to be extension of " + expectedClass.getSimpleName());
		}

		return expectedClass.cast(instance);
	}

	/**
	 * Get a type and cast it to a more specific type. Will throw
	 * {@link ModelException} if the type does not exist or if its of the wrong
	 * type.
	 *
	 * @param <T>
	 *   type
	 * @param def
	 *   type to get
	 * @param expectedClass
	 *   the expected class of the type
	 * @return
	 */
	default <T extends TypeDef> T getType(TypeDef def, Class<T> expectedClass)
	{
		TypeDef instance = def instanceof TypeRef ? getType(def.getName()) : def;
		if(! expectedClass.isAssignableFrom(instance.getClass()))
		{
			throw new ModelException("Expected type to be extension of " + expectedClass.getSimpleName());
		}

		return expectedClass.cast(instance);
	}
}
