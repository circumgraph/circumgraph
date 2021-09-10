package com.circumgraph.storage.scalars;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;

/**
 * Implementation of a {@link ScalarDef} as used for {@link SimpleValue values}.
 */
public interface Scalar<Java, GQL>
{
	/**
	 * Get the definition of the scalar.
	 *
	 * @return
	 */
	ScalarDef getModelType();

	/**
	 * Get the type as it exists in GraphQL form.
	 *
	 * @return
	 */
	Class<GQL> getGraphQLType();

	/**
	 * Get the type as it exists in Java form.
	 *
	 * @return
	 */
	Class<Java> getJavaType();

	/**
	 * Convert a Java object into the format used in GraphQL.
	 *
	 * @param in
	 *   object to be converted
	 * @return
	 *   converted value
	 * @throws ScalarConversionException
	 *   if conversion can not be done
	 */
	GQL toGraphQL(Java in);

	/**
	 * Convert a GraphQL object into the format used in the storage.
	 *
	 * @param in
	 *   object to convert, can be one of {@link String}, {@link Integer},
	 *   {@link Double}, {@link Boolean}, {@link java.util.Map} or
	 *   {@link java.util.List}
	 * @return
	 *   converted value
	 * @throws ScalarConversionException
	 *   if conversion can bot be done
	 */
	Java toJava(Object in);

	/**
	 * Read value from the given input.
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	Java read(StreamingInput in)
		throws IOException;

	/**
	 * Write value to the given output.
	 *
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	void write(Java object, StreamingOutput out)
		throws IOException;
}
