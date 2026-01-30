package io.github.omochice.pinosu.core.relay

import io.github.omochice.pinosu.core.model.NostrEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents messages received from a Nostr relay
 *
 * Nostr relay messages are JSON arrays where the first element indicates the message type.
 */
@Serializable(with = NostrRelayMessageSerializer::class)
sealed class NostrRelayMessage {
  /**
   * EVENT message containing a Nostr event
   *
   * Format: `["EVENT", subscriptionId, eventObject]`
   */
  data class Event(
      val subscriptionId: String,
      val event: NostrEvent,
  ) : NostrRelayMessage()

  /**
   * OK message indicating the result of publishing an event
   *
   * Format: `["OK", eventId, accepted, message?]`
   */
  data class Ok(
      val eventId: String,
      val accepted: Boolean,
      val message: String = "",
  ) : NostrRelayMessage()

  /**
   * EOSE (End of Stored Events) message
   *
   * Format: `["EOSE", subscriptionId]`
   */
  data class Eose(
      val subscriptionId: String,
  ) : NostrRelayMessage()

  /**
   * CLOSED message indicating subscription was closed
   *
   * Format: `["CLOSED", subscriptionId, message?]`
   */
  data class Closed(
      val subscriptionId: String,
      val message: String = "",
  ) : NostrRelayMessage()

  /**
   * Unknown message type
   *
   * Used for unrecognized message types to allow forward compatibility.
   */
  data class Unknown(
      val type: String,
      val raw: JsonArray,
  ) : NostrRelayMessage()
}

/**
 * Custom serializer for NostrRelayMessage
 *
 * Handles the array-based Nostr protocol message format.
 */
internal object NostrRelayMessageSerializer : KSerializer<NostrRelayMessage> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NostrRelayMessage")

  override fun serialize(encoder: Encoder, value: NostrRelayMessage) {
    throw SerializationException("Serialization of NostrRelayMessage is not supported")
  }

  override fun deserialize(decoder: Decoder): NostrRelayMessage {
    val jsonDecoder =
        decoder as? JsonDecoder
            ?: throw SerializationException("NostrRelayMessage can only be deserialized from JSON")

    val jsonArray = jsonDecoder.decodeJsonElement().jsonArray
    if (jsonArray.isEmpty()) {
      throw SerializationException("Empty message array")
    }

    val messageType =
        jsonArray[0].jsonPrimitive.contentOrNull
            ?: throw SerializationException("Message type must be a string")

    return when (messageType) {
      "EVENT" -> deserializeEvent(jsonArray, jsonDecoder)
      "OK" -> deserializeOk(jsonArray)
      "EOSE" -> deserializeEose(jsonArray)
      "CLOSED" -> deserializeClosed(jsonArray)
      else -> NostrRelayMessage.Unknown(messageType, jsonArray)
    }
  }

  private fun deserializeEvent(
      array: JsonArray,
      jsonDecoder: JsonDecoder,
  ): NostrRelayMessage.Event {
    if (array.size < 3) {
      throw SerializationException("EVENT message requires at least 3 elements")
    }
    val subscriptionId =
        array[1].jsonPrimitive.contentOrNull
            ?: throw SerializationException("Subscription ID must be a string")
    val event = jsonDecoder.json.decodeFromJsonElement(NostrEvent.serializer(), array[2])
    return NostrRelayMessage.Event(subscriptionId, event)
  }

  private fun deserializeOk(array: JsonArray): NostrRelayMessage.Ok {
    if (array.size < 3) {
      throw SerializationException("OK message requires at least 3 elements")
    }
    val eventId =
        array[1].jsonPrimitive.contentOrNull
            ?: throw SerializationException("Event ID must be a string")
    val accepted = array[2].jsonPrimitive.boolean
    val message = if (array.size > 3) array[3].jsonPrimitive.contentOrNull ?: "" else ""
    return NostrRelayMessage.Ok(eventId, accepted, message)
  }

  private fun deserializeEose(array: JsonArray): NostrRelayMessage.Eose {
    if (array.size < 2) {
      throw SerializationException("EOSE message requires at least 2 elements")
    }
    val subscriptionId =
        array[1].jsonPrimitive.contentOrNull
            ?: throw SerializationException("Subscription ID must be a string")
    return NostrRelayMessage.Eose(subscriptionId)
  }

  private fun deserializeClosed(array: JsonArray): NostrRelayMessage.Closed {
    if (array.size < 2) {
      throw SerializationException("CLOSED message requires at least 2 elements")
    }
    val subscriptionId =
        array[1].jsonPrimitive.contentOrNull
            ?: throw SerializationException("Subscription ID must be a string")
    val message = if (array.size > 2) array[2].jsonPrimitive.contentOrNull ?: "" else ""
    return NostrRelayMessage.Closed(subscriptionId, message)
  }
}
