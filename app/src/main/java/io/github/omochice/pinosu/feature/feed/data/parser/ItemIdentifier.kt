package io.github.omochice.pinosu.feature.feed.data.parser

import java.util.UUID

/** Chooses the identifier of a feed item that stays the same across fetches */
internal object ItemIdentifier {

  /**
   * Choose the identifier of an item
   *
   * @param declaredId Identifier the feed gives the item (RSS `guid`, Atom `id`), if any
   * @param link Item link, if any
   * @param title Item title
   * @param rawDate Item date exactly as written in the feed, if any
   * @return [declaredId], else [link], else a name-based UUID of [title] and [rawDate]
   */
  fun choose(declaredId: String?, link: String?, title: String, rawDate: String?): String =
      declaredId
          ?: link
          ?: UUID.nameUUIDFromBytes("$title\n${rawDate.orEmpty()}".toByteArray(Charsets.UTF_8))
              .toString()
}
