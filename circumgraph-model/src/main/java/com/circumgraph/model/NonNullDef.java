package com.circumgraph.model;

import com.circumgraph.model.internal.NonNullDefImpl;

/**
 * Type that represents something that may not be {@code null}.
 */
public interface NonNullDef
	extends TypeDef
{
	/**
	 * Get the type that may not be {@code null}.
	 *
	 * @return
	 */
	TypeDef getType();

	/**
	 * Get the name of the type.
	 *
	 * @return
	 */
	String getTypeName();

	/**
	 * Version used for output.
	 */
	interface Output
		extends NonNullDef, OutputTypeDef
	{
		@Override
		OutputTypeDef getType();
	}

	/**
	 * Version used for input.
	 */
	interface Input
		extends NonNullDef, InputTypeDef
	{
		@Override
		InputTypeDef getType();
	}

	/**
	 * Create a non-null version of the given output type.
	 *
	 * @param type
	 * @return
	 */
	static NonNullDef.Output output(String type)
	{
		return new NonNullDefImpl.OutputImpl(TypeRef.create(type));
	}

	/**
	 * Create a non-null version of the given output type.
	 *
	 * @param type
	 * @return
	 */
	static NonNullDef.Output output(OutputTypeDef type)
	{
		return new NonNullDefImpl.OutputImpl(type);
	}

	/**
	 * Create a non-null version of the given input type.
	 *
	 * @param type
	 * @return
	 */
	static NonNullDef.Input input(String type)
	{
		return new NonNullDefImpl.InputImpl(TypeRef.create(type));
	}

	/**
	 * Create a non-null version of the given input type.
	 *
	 * @param type
	 * @return
	 */
	static NonNullDef.Input input(InputTypeDef type)
	{
		return new NonNullDefImpl.InputImpl(type);
	}
}
