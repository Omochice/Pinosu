package io.github.omochice.pinosu.core.relay

import io.github.omochice.pinosu.core.model.NostrEvent
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocket mock that answers a REQ with relay messages built from the subscription ID
 *
 * Callbacks run on [executor] after short delays so that the code under test is already waiting
 * when they arrive, as with a real relay.
 *
 * @param executor Executor that delivers listener callbacks
 * @param closeAfterScript Whether the relay drops the connection after the last message instead of
 *   waiting for the client to close it
 * @param script Relay messages to send in reply to a REQ, given its subscription ID
 */
internal class ScriptedRelayWebSocket(
    private val executor: Executor,
    private val closeAfterScript: Boolean = false,
    private val script: (subscriptionId: String) -> List<String>,
) {
  val ws: WebSocket = mockk(relaxed = true)

  /** Messages the client sent over this socket, in order */
  val sentMessages = CopyOnWriteArrayList<String>()
  private val closed = AtomicBoolean(false)

  @Volatile private var wsListener: WebSocketListener? = null

  init {
    every { ws.send(any<String>()) } answers
        {
          val message = firstArg<String>()
          sentMessages.add(message)
          val currentListener = wsListener
          if (currentListener != null && message.startsWith("[\"REQ\"")) {
            replyTo(currentListener, extractSubscriptionId(message))
          }
          true
        }

    every { ws.close(any(), any()) } answers
        {
          closed.set(true)
          val code = firstArg<Int>()
          val reason = secondArg<String?>() ?: ""
          val currentListener = wsListener
          if (currentListener != null) {
            executor.execute {
              Thread.sleep(CLOSE_DELAY_MS)
              currentListener.onClosed(ws, code, reason)
            }
          }
          true
        }
  }

  private fun replyTo(listener: WebSocketListener, subscriptionId: String) {
    executor.execute {
      Thread.sleep(REPLY_DELAY_MS)
      for (reply in script(subscriptionId)) {
        if (!closed.get()) {
          listener.onMessage(ws, reply)
          Thread.sleep(MESSAGE_INTERVAL_MS)
        }
      }
      if (closeAfterScript && !closed.get()) {
        listener.onClosing(ws, 1001, "going away")
      }
    }
  }

  private fun extractSubscriptionId(reqMessage: String): String {
    val regex = """\["REQ","([^"]+)".*\]""".toRegex()
    return regex.find(reqMessage)?.groupValues?.get(1) ?: "unknown"
  }

  /**
   * Attach [listener] and open the connection
   *
   * @return The mocked socket to hand back from [OkHttpClient.newWebSocket]
   */
  fun connect(listener: WebSocketListener): WebSocket {
    wsListener = listener
    executor.execute {
      Thread.sleep(OPEN_DELAY_MS)
      listener.onOpen(ws, mockk<Response>(relaxed = true))
    }
    return ws
  }

  private companion object {
    const val OPEN_DELAY_MS = 30L
    const val REPLY_DELAY_MS = 50L
    const val MESSAGE_INTERVAL_MS = 10L
    const val CLOSE_DELAY_MS = 30L
  }
}

/** Route connections to URLs containing [urlPart] to [state] */
internal fun OkHttpClient.stubRelay(urlPart: String, state: ScriptedRelayWebSocket) {
  every { newWebSocket(match { it.url.toString().contains(urlPart) }, any()) } answers
      {
        state.connect(secondArg())
      }
}

/** Make connections to URLs containing [urlPart] fail immediately with [message] */
internal fun OkHttpClient.stubFailingRelay(urlPart: String, message: String) {
  every { newWebSocket(match { it.url.toString().contains(urlPart) }, any()) } answers
      {
        val ws = mockk<WebSocket>(relaxed = true)
        secondArg<WebSocketListener>().onFailure(ws, IOException(message), null)
        ws
      }
}

/** Build a signed-looking event whose only meaningful field is [id] */
internal fun relayTestEvent(id: String): NostrEvent =
    NostrEvent(
        id = id,
        pubkey = "testpubkey",
        createdAt = 1_700_000_000L,
        kind = 1,
        tags = listOf(listOf("d", "test")),
        content = "",
        sig = "dummy-sig")

/** Build the relay's EVENT message delivering [event] for [subscriptionId] */
internal fun eventMessage(subscriptionId: String, event: NostrEvent): String {
  val eventJson = Json.encodeToString(NostrEvent.serializer(), event)
  return """["EVENT","$subscriptionId",$eventJson]"""
}

/** Build the relay's EOSE message for [subscriptionId] */
internal fun eoseMessage(subscriptionId: String): String = """["EOSE","$subscriptionId"]"""
