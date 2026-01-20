package io.github.omochice.pinosu.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarkDisplayModeTest {

  @Test
  fun `enum has List and Grid values`() {
    val values = BookmarkDisplayMode.entries
    assertEquals(2, values.size)
    assertEquals(BookmarkDisplayMode.List, values[0])
    assertEquals(BookmarkDisplayMode.Grid, values[1])
  }

  @Test
  fun `List is the first entry for use as default`() {
    assertEquals(BookmarkDisplayMode.List, BookmarkDisplayMode.entries.first())
  }
}
