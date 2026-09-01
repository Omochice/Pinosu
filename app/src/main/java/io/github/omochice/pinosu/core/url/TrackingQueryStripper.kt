package io.github.omochice.pinosu.core.url

private val trackingKeys =
    setOf(
        "fbclid",
        "gclid",
        "dclid",
        "gbraid",
        "wbraid",
        "msclkid",
        "mc_eid",
        "yclid",
        "ysclid",
        "twclid",
        "igshid",
        "_hsenc",
        "_hsmi",
        "__hssc",
        "__hstc",
        "__hsfp",
        "hsctatracking",
        "wickedid",
        "_openstat",
        "mkt_tok",
        "oly_anon_id",
        "oly_enc_id",
        "vero_id",
        "__s",
    )

private const val TRACKING_KEY_PREFIX = "utm_"

/**
 * Removes known tracking query parameters from [url], leaving every surviving part byte for byte as
 * it was. The key list and the design rationale are recorded in
 * doc/adr/strip-tracking-query-params.md.
 *
 * @param url The URL to clean; scheme-less input and input without a query are returned unchanged.
 * @return The URL with tracking parameters removed.
 */
fun stripTrackingQueryParameters(url: String): String {
  val hashIndex = url.indexOf('#')
  val queryIndex = url.indexOf('?')
  if (queryIndex == -1 || hashIndex != -1 && hashIndex < queryIndex) {
    return url
  }
  val queryEnd = if (hashIndex == -1) url.length else hashIndex
  val surviving =
      url.substring(queryIndex + 1, queryEnd).split("&").filterNot { segment ->
        val key = segment.substringBefore('=').lowercase()
        key.startsWith(TRACKING_KEY_PREFIX) || key in trackingKeys
      }
  val query = if (surviving.isEmpty()) "" else "?" + surviving.joinToString("&")
  return url.substring(0, queryIndex) + query + url.substring(queryEnd)
}
