package com.circumgraph.model.internal;

import com.circumgraph.model.ModelException;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;

@FunctionalInterface
public interface ModelDefs
{
	TypeDef maybeResolve(String id);

	default TypeDef getType(String id)
	{
		TypeDef type = maybeResolve(id);
		if(type == null)
		{
			throw new ModelException("Can not find type with id " + id);
		}

		return type;
	}

	default <T extends TypeDef> T getType(String id, Class<T> type)
	{
		TypeDef instance = getType(id);
		if(! type.isAssignableFrom(instance.getClass()))
		{
			throw new ModelException("Expected type to be extension of " + type.getSimpleName());
		}

		return type.cast(instance);
	}

	default <T extends TypeDef> T getType(TypeDef def, Class<T> type)
	{
		TypeDef instance = def instanceof TypeRef ? getType(def.getName()) : def;
		if(! type.isAssignableFrom(instance.getClass()))
		{
			throw new ModelException("Expected type to be extension of " + type.getSimpleName());
		}

		return type.cast(instance);
	}
}
