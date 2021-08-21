package com.circumgraph.model;

import java.util.Objects;

import com.circumgraph.model.internal.ListDefImpl;

/**
 * Type that represents a list of a certain {@link #getItemType() type}.
 */
public interface ListDef
	extends TypeDef
{
	/**
	 * Get the type of data in this list.
	 *
	 * @return
	 */
	TypeDef getItemType();

	/**
	 * Get the name of the type of data this list contains.
	 *
	 * @return
	 */
	String getItemTypeName();

	/**
	 * List that is used for output.
	 */
	interface Output
		extends ListDef, OutputTypeDef
	{
		@Override
		OutputTypeDef getItemType();
	}

	/**
	 * List that is used for input.
	 */
	interface Input
		extends ListDef, InputTypeDef
	{
		@Override
		InputTypeDef getItemType();
	}

	/**
	 * Create a list for an output type.
	 *
	 * @param type
	 * @return
	 */
	static ListDef.Output output(String type)
	{
		Objects.requireNonNull(type);
		return new ListDefImpl.OutputImpl(TypeRef.create(type));
	}

	/**
	 * Create a list for an output type.
	 *
	 * @param type
	 * @return
	 */
	static ListDef.Output output(OutputTypeDef type)
	{
		Objects.requireNonNull(type);
		return new ListDefImpl.OutputImpl(type);
	}

	/**
	 * Create a list for an input type.
	 *
	 * @param type
	 * @return
	 */
	static ListDef.Input input(String type)
	{
		Objects.requireNonNull(type);
		return new ListDefImpl.InputImpl(TypeRef.create(type));
	}

	/**
	 * Create a list for an input type.
	 *
	 * @param type
	 * @return
	 */
	static ListDef.Input input(InputTypeDef type)
	{
		Objects.requireNonNull(type);
		return new ListDefImpl.InputImpl(type);
	}
}
