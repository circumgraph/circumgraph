package com.circumgraph.model;

import com.circumgraph.model.internal.ScalarDefImpl;

/**
 * Scalar representation.
 */
public interface ScalarDef
	extends SimpleValueDef
{
	/**
	 * String scalar.
	 */
	static final ScalarDef STRING = new ScalarDefImpl("String", null);

	/**
	 * Int scalar.
	 */
	static final ScalarDef INT = new ScalarDefImpl("Int", null);

	/**
	 * Float scalar.
	 */
	static final ScalarDef FLOAT = new ScalarDefImpl("Float", null);

	/**
	 * Boolean scalar.
	 */
	static final ScalarDef BOOLEAN = new ScalarDefImpl("Boolean", null);

	/**
	 * ID scalar.
	 */
	static final ScalarDef ID = new ScalarDefImpl("ID", null);

	/**
	 * LocalDate scalar.
	 */
	static final ScalarDef LOCAL_DATE = new ScalarDefImpl("LocalDate", "Date without a time zone");

	/**
	 * LocalTime scalar.
	 */
	static final ScalarDef LOCAL_TIME = new ScalarDefImpl("LocalTime", "Time without a date or time zone");

	/**
	 * LocalDateTime scalar.
	 */
	static final ScalarDef LOCAL_DATE_TIME = new ScalarDefImpl("LocalDateTime", "Date and time without a time zone");

	static final ScalarDef ZONED_DATE_TIME = new ScalarDefImpl("ZonedDateTime", "Date and time with a full time zone");

	static final ScalarDef DURATION = new ScalarDefImpl("Duration", "Duration of time");

	static final ScalarDef OFFSET_TIME = new ScalarDefImpl("OffsetTime", "Time with a zone offset");

	static final ScalarDef OFFSET_DATE_TIME = new ScalarDefImpl("OffsetDateTime", "Date and time with a zone offset");
}
