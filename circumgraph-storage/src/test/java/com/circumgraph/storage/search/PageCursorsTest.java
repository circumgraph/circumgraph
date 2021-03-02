package com.circumgraph.storage.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.storage.internal.search.OffsetBasedCursor;
import com.circumgraph.storage.internal.search.PageCursorImpl;
import com.circumgraph.storage.internal.search.PageCursorsImpl;

import org.junit.jupiter.api.Test;

public class PageCursorsTest
{
	@Test
	public void testPreviousPageExists()
	{
		var cursors = new PageCursorsImpl(10, 12, 10, 7);
		var previous = cursors.getPrevious();

		assertThat(previous, notNullValue());
		assertThat(previous.getPageNumber(), is(1));
		assertThat(previous.getCursor(), is(new OffsetBasedCursor(0)));
		assertThat(previous.isCurrent(), is(false));
	}

	@Test
	public void testPreviousPageMissing()
	{
		var cursors = new PageCursorsImpl(0, 12, 10, 7);
		var previous = cursors.getPrevious();

		assertThat(previous, nullValue());
	}

	@Test
	public void testNextPageExists()
	{
		var cursors = new PageCursorsImpl(0, 12, 10, 7);
		var next = cursors.getNext();

		assertThat(next, notNullValue());
		assertThat(next.getPageNumber(), is(2));
		assertThat(next.getCursor(), is(new OffsetBasedCursor(10)));
		assertThat(next.isCurrent(), is(false));
	}

	@Test
	public void testNextPageMissing()
	{
		var cursors = new PageCursorsImpl(10, 12, 10, 7);
		var next = cursors.getNext();

		assertThat(next, nullValue());
	}

	@Test
	public void testStart1()
	{
		var cursors = new PageCursorsImpl(10, 32, 10, 7);

		assertThat(cursors.getStart(), contains(
			new PageCursorImpl(new OffsetBasedCursor(0), 1, false),
			new PageCursorImpl(new OffsetBasedCursor(10), 2, true),
			new PageCursorImpl(new OffsetBasedCursor(20), 3, false),
			new PageCursorImpl(new OffsetBasedCursor(30), 4, false)
		));

		assertThat(cursors.getMiddle().isEmpty(), is(true));

		assertThat(cursors.getEnd().isEmpty(), is(true));
	}

	@Test
	public void testStart2()
	{
		var cursors = new PageCursorsImpl(10, 132, 10, 7);

		assertThat(cursors.getStart(), contains(
			new PageCursorImpl(new OffsetBasedCursor(0), 1, false),
			new PageCursorImpl(new OffsetBasedCursor(10), 2, true),
			new PageCursorImpl(new OffsetBasedCursor(20), 3, false),
			new PageCursorImpl(new OffsetBasedCursor(30), 4, false),
			new PageCursorImpl(new OffsetBasedCursor(40), 5, false)
		));

		assertThat(cursors.getMiddle().isEmpty(), is(true));

		assertThat(cursors.getEnd(), contains(
			new PageCursorImpl(new OffsetBasedCursor(130), 14, false)
		));
	}

	@Test
	public void testEnd1()
	{
		var cursors = new PageCursorsImpl(110, 132, 10, 7);

		assertThat(cursors.getStart(), contains(
			new PageCursorImpl(new OffsetBasedCursor(0), 1, false)
		));

		assertThat(cursors.getMiddle().isEmpty(), is(true));

		assertThat(cursors.getEnd(), contains(
			new PageCursorImpl(new OffsetBasedCursor(90), 10, false),
			new PageCursorImpl(new OffsetBasedCursor(100), 11, false),
			new PageCursorImpl(new OffsetBasedCursor(110), 12, true),
			new PageCursorImpl(new OffsetBasedCursor(120), 13, false),
			new PageCursorImpl(new OffsetBasedCursor(130), 14, false)
		));
	}

	@Test
	public void testEnd2()
	{
		var cursors = new PageCursorsImpl(110, 132, 10, 9);

		assertThat(cursors.getStart(), contains(
			new PageCursorImpl(new OffsetBasedCursor(0), 1, false)
		));

		assertThat(cursors.getMiddle().isEmpty(), is(true));

		assertThat(cursors.getEnd(), contains(
			new PageCursorImpl(new OffsetBasedCursor(70), 8, false),
			new PageCursorImpl(new OffsetBasedCursor(80), 9, false),
			new PageCursorImpl(new OffsetBasedCursor(90), 10, false),
			new PageCursorImpl(new OffsetBasedCursor(100), 11, false),
			new PageCursorImpl(new OffsetBasedCursor(110), 12, true),
			new PageCursorImpl(new OffsetBasedCursor(120), 13, false),
			new PageCursorImpl(new OffsetBasedCursor(130), 14, false)
		));
	}

	@Test
	public void testMiddle1()
	{
		var cursors = new PageCursorsImpl(110, 232, 10, 9);

		assertThat(cursors.getStart(), contains(
			new PageCursorImpl(new OffsetBasedCursor(0), 1, false)
		));

		assertThat(cursors.getMiddle(), contains(
			new PageCursorImpl(new OffsetBasedCursor(90), 10, false),
			new PageCursorImpl(new OffsetBasedCursor(100), 11, false),
			new PageCursorImpl(new OffsetBasedCursor(110), 12, true),
			new PageCursorImpl(new OffsetBasedCursor(120), 13, false),
			new PageCursorImpl(new OffsetBasedCursor(130), 14, false)
		));

		assertThat(cursors.getEnd(), contains(
			new PageCursorImpl(new OffsetBasedCursor(230), 24, false)
		));
	}
}
