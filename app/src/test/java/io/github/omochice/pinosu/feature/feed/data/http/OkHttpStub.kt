package io.github.omochice.pinosu.feature.feed.data.http

import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * A mocked [OkHttpClient] that answers each URL with a canned response and records every request
 *
 * Requests to URLs without a canned response fail with an [IOException], the way an unreachable
 * host does.
 */
internal class OkHttpStub {

  private val routes = mutableMapOf<String, (Request) -> Response>()

  /** Every request the client was asked to execute, in order */
  val requests = mutableListOf<Request>()

  val client: OkHttpClient = mockk {
    every { newCall(any()) } answers
        {
          val request = firstArg<Request>()
          mockk<Call> {
            every { execute() } answers
                {
                  requests += request
                  val route =
                      routes[request.url.toString()]
                          ?: throw IOException("Unreachable: ${request.url}")
                  route(request)
                }
          }
        }
  }

  fun respond(
      url: String,
      body: String,
      contentType: String = "text/html; charset=utf-8",
      code: Int = 200,
      finalUrl: String = url,
  ) {
    respondWith(url, body.toResponseBody(contentType.toMediaType()), code, finalUrl)
  }

  fun respondWith(url: String, body: ResponseBody, code: Int = 200, finalUrl: String = url) {
    routes[url] = { request ->
      Response.Builder()
          .request(request.newBuilder().url(finalUrl).build())
          .protocol(Protocol.HTTP_1_1)
          .code(code)
          .message("status $code")
          .body(body)
          .build()
    }
  }

  fun fail(url: String, error: IOException) {
    routes[url] = { throw error }
  }
}
