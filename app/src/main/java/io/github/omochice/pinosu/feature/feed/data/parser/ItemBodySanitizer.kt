package io.github.omochice.pinosu.feature.feed.data.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

/**
 * Reduces the HTML body of a feed item to text formatting and tables with absolute HTTP(S) links
 */
internal object ItemBodySanitizer {

  private const val ANCHOR = "a"
  private const val HREF = "href"
  private const val COLSPAN = "colspan"
  private const val ROWSPAN = "rowspan"

  /**
   * Spans beyond this are dropped rather than kept, because each spanned column or row is laid out
   * even when it holds nothing, so one cell could otherwise blow up the size of the table.
   */
  private const val MAX_SPAN = 20

  /**
   * [Safelist.basic] drops headings and `div`, which would run separate blocks of an article
   * together, and tables, which would run their cells together, so they are added.
   *
   * [Safelist.relaxed] is not used for tables, because it also lets images through.
   */
  private val SAFELIST: Safelist =
      Safelist.basic()
          .addTags("div", "h1", "h2", "h3", "h4", "h5", "h6")
          .addTags("table", "caption", "thead", "tbody", "tfoot", "tr", "th", "td")
          .addAttributes("th", COLSPAN, ROWSPAN)
          .addAttributes("td", COLSPAN, ROWSPAN)
          .removeProtocols(ANCHOR, HREF, "ftp", "mailto")
          .removeEnforcedAttribute(ANCHOR, "rel")

  /**
   * Sanitize the HTML body of an item
   *
   * @param html Body as HTML
   * @param base URL that relative links in [html] are relative to
   * @return The sanitized HTML, or null when no text is left
   */
  fun sanitize(html: String, base: String): String? {
    val dirty = Jsoup.parseBodyFragment(html)
    resolveLinks(dirty, base)
    val clean = Cleaner(SAFELIST).clean(dirty)
    limitSpans(clean)
    clean.outputSettings().prettyPrint(false)
    return clean.body().takeIf { it.hasText() }?.html()
  }

  /**
   * Links are resolved by [LinkResolver] rather than by the cleaner, so that the same rules as for
   * item links decide which schemes survive, and a `base` element in the body cannot move the base.
   */
  private fun resolveLinks(document: Document, base: String) {
    document.select("$ANCHOR[$HREF]").forEach { anchor ->
      val resolved = LinkResolver.resolveAgainst(base, anchor.attr(HREF).trim())
      if (resolved == null) {
        anchor.removeAttr(HREF)
      } else {
        anchor.attr(HREF, resolved)
      }
    }
  }

  private fun limitSpans(document: Document) {
    listOf(COLSPAN, ROWSPAN).forEach { attribute ->
      document.select("[$attribute]").forEach { cell ->
        val span = cell.attr(attribute).trim().toIntOrNull()?.takeIf { it in 1..MAX_SPAN }
        if (span == null) {
          cell.removeAttr(attribute)
        } else {
          cell.attr(attribute, span.toString())
        }
      }
    }
  }
}
