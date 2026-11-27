package io.github.omochice.pinosu.feature.feed.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** DataStore serializer storing [ArticleImageLookups] as JSON */
object ArticleImageLookupsSerializer : Serializer<ArticleImageLookups> {

  override val defaultValue: ArticleImageLookups = ArticleImageLookups()

  override suspend fun readFrom(input: InputStream): ArticleImageLookups =
      try {
        Json.decodeFromString<ArticleImageLookups>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
        throw CorruptionException("Failed to deserialize stored article images", e)
      }

  override suspend fun writeTo(t: ArticleImageLookups, output: OutputStream) {
    output.write(Json.encodeToString(t).encodeToByteArray())
  }
}
