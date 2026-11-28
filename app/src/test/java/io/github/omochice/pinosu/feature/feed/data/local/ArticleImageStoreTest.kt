package io.github.omochice.pinosu.feature.feed.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

class ArticleImageStoreTest {

  private val storeFile = ArticleImageDataStoreFile()

  @AfterTest fun tearDown() = runBlocking { storeFile.delete() }

  @Test
  fun `a stored lookup is found by a later run of the app`() = runTest {
    ArticleImageStore(storeFile.open()).save(LINK, ArticleImageLookup(IMAGE, 1L))

    val found = ArticleImageStore(storeFile.open()).find(LINK)

    assertEquals(ArticleImageLookup(IMAGE, 1L), found)
  }

  @Test
  fun `a page without an image is stored as having none`() = runTest {
    val store = ArticleImageStore(storeFile.open())

    store.save(LINK, ArticleImageLookup(null, 1L))

    assertEquals(ArticleImageLookup(null, 1L), store.find(LINK))
  }

  @Test
  fun `a link never stored is not found`() = runTest {
    assertNull(ArticleImageStore(storeFile.open()).find(LINK))
  }

  @Test
  fun `a newer lookup of the same link replaces the older one`() = runTest {
    val store = ArticleImageStore(storeFile.open())

    store.save(LINK, ArticleImageLookup(null, 1L))
    store.save(LINK, ArticleImageLookup(IMAGE, 2L))

    assertEquals(ArticleImageLookup(IMAGE, 2L), store.find(LINK))
  }

  @Test
  fun `only the most recent lookups are kept`() = runTest {
    val store = ArticleImageStore(storeFile.open())

    (0..ArticleImageStore.MAX_LOOKUPS).forEach { n ->
      store.save("$LINK/$n", ArticleImageLookup(IMAGE, n.toLong()))
    }

    assertNull(store.find("$LINK/0"), "the oldest lookup is forgotten")
    assertEquals(ArticleImageLookup(IMAGE, 1L), store.find("$LINK/1"))
    assertEquals(ArticleImageStore.MAX_LOOKUPS, storeFile.open().data.first().lookups.size)
  }

  @Test
  fun `clearing forgets every lookup`() = runTest {
    val store = ArticleImageStore(storeFile.open())
    store.save(LINK, ArticleImageLookup(IMAGE, 1L))

    store.clear()

    assertNull(store.find(LINK))
    assertNull(ArticleImageStore(storeFile.open()).find(LINK))
  }

  @Test
  fun `a store that cannot be read is taken as holding nothing`() = runTest {
    val unreadable =
        mockk<DataStore<ArticleImageLookups>> {
          every { data } returns flow { throw IOException("disk error") }
        }

    assertNull(ArticleImageStore(unreadable).find(LINK))
  }

  @Test
  fun `a store that cannot be written does not fail the lookup`() = runTest {
    val unwritable =
        mockk<DataStore<ArticleImageLookups>> {
          coEvery { updateData(any()) } throws IOException("disk full")
        }

    ArticleImageStore(unwritable).save(LINK, ArticleImageLookup(IMAGE, 1L))
  }

  @Test
  fun `a file that is not valid json is reported as corrupt`() = runTest {
    assertFailsWith<CorruptionException> {
      ArticleImageLookupsSerializer.readFrom(ByteArrayInputStream("{not json".toByteArray()))
    }
  }

  private companion object {
    const val LINK = "https://example.com/article"
    const val IMAGE = "https://example.com/og.png"
  }
}
