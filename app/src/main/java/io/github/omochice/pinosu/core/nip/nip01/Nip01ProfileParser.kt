package io.github.omochice.pinosu.core.nip.nip01

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UserProfile
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parser for NIP-01 user metadata events (kind 0)
 *
 * Kind 0 events contain a JSON string in the content field with profile metadata fields like name,
 * picture, and about.
 */
interface Nip01ProfileParser {

  /**
   * Parse a kind 0 event into a UserProfile
   *
   * @param event The NostrEvent to parse (expected kind 0)
   * @return UserProfile extracted from the event content, or null if parsing fails or event is not
   *   kind 0
   */
  fun parseProfileEvent(event: NostrEvent): UserProfile?
}

/** Implementation of [Nip01ProfileParser] */
class Nip01ProfileParserImpl @Inject constructor() : Nip01ProfileParser {

  override fun parseProfileEvent(event: NostrEvent): UserProfile? {
    if (event.kind != KIND_USER_METADATA) {
      return null
    }

    val content =
        try {
          lenientJson.decodeFromString<ProfileContent>(event.content)
        } catch (_: Exception) {
          return null
        }

    return UserProfile(
        pubkey = event.pubkey,
        name = content.name,
        picture = content.picture,
        about = content.about,
    )
  }

  @Serializable
  private data class ProfileContent(
      @SerialName("name") val name: String? = null,
      @SerialName("picture") val picture: String? = null,
      @SerialName("about") val about: String? = null,
  )

  companion object {
    const val KIND_USER_METADATA = 0

    private val lenientJson = Json { ignoreUnknownKeys = true }
  }
}
